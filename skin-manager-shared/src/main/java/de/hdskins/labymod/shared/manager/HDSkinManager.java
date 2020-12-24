/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.hdskins.labymod.shared.manager;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.concurrent.ConcurrentUtil;
import de.hdskins.labymod.shared.concurrent.FunctionalFutureListener;
import de.hdskins.labymod.shared.listener.ClientListeners;
import de.hdskins.labymod.shared.listener.NetworkListeners;
import de.hdskins.labymod.shared.resource.HDResourceLocation;
import de.hdskins.labymod.shared.texture.HDMinecraftProfileTexture;
import de.hdskins.labymod.shared.texture.HDSkinTexture;
import de.hdskins.labymod.shared.utils.GameProfileUtils;
import de.hdskins.labymod.shared.utils.MCUtil;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketClientReady;
import de.hdskins.protocol.packets.reading.download.PacketClientRequestSkin;
import de.hdskins.protocol.packets.reading.download.PacketClientRequestSkinId;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkinId;
import de.hdskins.protocol.packets.reading.live.PacketClientLiveSkinUnload;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@SuppressWarnings("UnstableApiUsage")
public class HDSkinManager extends SkinManager {

  private static final SkinHashWrapper NO_SKIN = SkinHashWrapper.newEmpty();
  private static final Logger LOGGER = LogManager.getLogger(HDSkinManager.class);
  private static final Collection<RemovalCause> HANDLED_CAUSES = EnumSet.range(RemovalCause.COLLECTED, RemovalCause.SIZE);

  private final Path assetsDirectory;
  private final AddonContext addonContext;
  private final Consumer<UUID> skinInvalidator;
  private final Queue<UUID> nonSentUnloads = new ConcurrentLinkedQueue<>();
  private final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
  private final LoadingCache<MinecraftProfileTexture, HDResourceLocation> textureToLocationCache = CacheBuilder.newBuilder()
    .expireAfterAccess(30, TimeUnit.SECONDS)
    .concurrencyLevel(4)
    .ticker(Ticker.systemTicker())
    .build(new CacheLoader<MinecraftProfileTexture, HDResourceLocation>() {
      @Override
      public HDResourceLocation load(@Nonnull MinecraftProfileTexture texture) {
        return HDResourceLocation.forProfileTexture(texture);
      }
    });
  private final LoadingCache<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> mojangProfileCache = CacheBuilder.newBuilder()
    .expireAfterAccess(30, TimeUnit.SECONDS)
    .concurrencyLevel(4)
    .ticker(Ticker.systemTicker())
    .build(new CacheLoader<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {
      @Override
      public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(@Nonnull GameProfile gameProfile) {
        return Minecraft.getMinecraft().getSessionService().getTextures(gameProfile, false);
      }
    });
  private final Cache<UUID, SkinHashWrapper> uniqueIdToSkinHashCache = CacheBuilder.newBuilder()
    .expireAfterAccess(30, TimeUnit.SECONDS)
    .removalListener(this::handleRemove)
    .concurrencyLevel(4)
    .ticker(Ticker.systemTicker())
    .build();

  public HDSkinManager(AddonContext addonContext, File mcAssetsDir, Consumer<UUID> skinInvalidator) {
    super(Minecraft.getMinecraft().getTextureManager(), new File(mcAssetsDir, "skins"), Minecraft.getMinecraft().getSessionService());
    this.assetsDirectory = mcAssetsDir.toPath();
    this.addonContext = addonContext;
    this.skinInvalidator = skinInvalidator;
    // Register listeners to this skin manager
    addonContext.getNetworkClient().getPacketListenerRegistry().registerListeners(new NetworkListeners(this, addonContext.getTranslationRegistry()));
    // We are ready
    addonContext.getNetworkClient().sendPacket(new PacketClientReady());
    // Register client listeners to forge & internal event bus
    final ClientListeners clientListeners = new ClientListeners(this);
    LabyMod.getInstance().getLabyModAPI().registerForgeListener(clientListeners);
    Constants.EVENT_BUS.registerListener(clientListeners);
  }

  @Override
  public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type) {
    return this.loadSkin(texture, type, null);
  }

  @Override
  public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type, @Nullable SkinAvailableCallback callback) {
    if (!(texture instanceof HDMinecraftProfileTexture)) {
      return super.loadSkin(texture, type, callback);
    }
    // Build a resource location for the profile texture
    final HDResourceLocation location = this.textureToLocationCache.getUnchecked(texture);
    // Test if the texture is already loaded and cached
    final ITextureObject textureObject = this.textureManager.getTexture(location);
    // If the texture object is non-null we can break now
    if (textureObject != null) {
      // If a callback is provided by the method we should post the result to it
      if (callback != null) {
        MCUtil.call(ConcurrentUtil.fromRunnable(() -> callback.skinAvailable(type, location, texture)));
      }
      return location;
    }
    // Check if the file locally already exits
    final Path localSkinPath = this.assetsDirectory.resolve(location.getPath());
    if (Files.exists(localSkinPath)) {
      try {
        if (this.textureManager.loadTexture(location, new HDSkinTexture(localSkinPath))) {
          // If a callback is provided by the method we should post the result to it
          if (callback != null) {
            MCUtil.call(ConcurrentUtil.fromRunnable(() -> callback.skinAvailable(type, location, texture)));
          }
          return location;
        } else {
          // unreachable but well
          Files.deleteIfExists(localSkinPath);
        }
      } catch (IOException exception) {
        LOGGER.debug("Tried to load skin {} from local path {} but failed", location, localSkinPath, exception);
      }
    }
    // We were disconnected from the server so we cannot load a skin from there
    if (!this.addonContext.getActive().get()) {
      // If we are not reconnecting already, begin now
      if (!this.addonContext.getReconnecting().getAndSet(true)) {
        BackendUtils.reconnect(this.addonContext).thenRun(() -> {
          this.addonContext.getActive().set(true);
          this.addonContext.getReconnecting().set(false);
        });
      }
      // As we are not connected to the server we try a legacy lookup
      return super.loadSkin(texture, type, callback);
    }
    // We ensured that the skin is not available locally so try to load it from the server
    this.addonContext.getNetworkClient().sendQuery(new PacketClientRequestSkin(texture.getHash())).addListener(this.forSkinLoad(texture, type, callback, location, localSkinPath));
    return location;
  }

  @Override
  public void loadProfileTextures(GameProfile profile, SkinAvailableCallback callback, boolean requireSecure) {
    if (profile.getId() == null) {
      LOGGER.debug("Unable to load skin for profile: {} callback: {} secure: {} because profile has no unique id", profile, callback, requireSecure);
      super.loadProfileTextures(profile, callback, requireSecure);
      return;
    }

    final UUID self = LabyMod.getInstance().getPlayerUUID();
    final UUID profileId = GameProfileUtils.getUniqueId(profile);

    if (profileId == null || (!profileId.equals(self) && !this.addonContext.getAddonConfig().showSkinsOfOtherPlayers()) || this.addonContext.getAddonConfig().isSkinDisabled(profileId)) {
      LOGGER.debug("Not loading skin for profile: {} callback: {} secure: {} because the unique id is blocked locally.", profile, callback, requireSecure);
      super.loadProfileTextures(profile, callback, requireSecure);
      return;
    }

    final SkinHashWrapper response = this.uniqueIdToSkinHashCache.getIfPresent(profileId);
    if (response == null) {
      // We were disconnected from the server so we cannot load a skin from there
      if (!this.addonContext.getActive().get()) {
        // If we are not reconnecting already, begin now
        if (!this.addonContext.getReconnecting().getAndSet(true)) {
          BackendUtils.reconnect(this.addonContext).thenRun(() -> {
            this.addonContext.getActive().set(true);
            this.addonContext.getReconnecting().set(false);
          });
        }
        // As we are not connected to the server we try a legacy lookup
        super.loadProfileTextures(profile, callback, requireSecure);
        return;
      }
      // We send a query to the server to get the skin hash from the uuid given
      this.addonContext.getNetworkClient()
        .sendQuery(new PacketClientRequestSkinId(profileId))
        .addListener(this.newListenerForSkinIdLoad(profileId, profile, callback, requireSecure));
    } else if (response.hasSkin()) {
      // We already sent a request to the server so we can now simply load the texture by the hash we already got from the server
      this.loadSkin(response.toProfileTexture(), MinecraftProfileTexture.Type.SKIN, callback);
    } else {
      // The player has no hd skin so we do a legacy load
      super.loadProfileTextures(profile, callback, requireSecure);
    }
  }

  @Override
  public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
    final UUID profileId = GameProfileUtils.getUniqueId(profile);
    if (profileId == null) {
      LOGGER.debug("Unable to load skin {} from cache because skin unique id is null", profile);
      if (profile.getProperties().containsKey("textures")) {
        return this.mojangProfileCache.getUnchecked(profile);
      } else {
        return ImmutableMap.of();
      }
    }

    final SkinHashWrapper response = this.uniqueIdToSkinHashCache.getIfPresent(profileId);
    if (response != null) {
      if (!response.hasSkin()) {
        if (profile.getProperties().containsKey("textures")) {
          return this.mojangProfileCache.getUnchecked(profile);
        } else {
          return ImmutableMap.of();
        }
      }
      return ImmutableMap.of(MinecraftProfileTexture.Type.SKIN, response.toProfileTexture());
    }
    if (this.addonContext.getActive().get()) {
      this.addonContext.getNetworkClient().sendQuery(new PacketClientRequestSkinId(profileId)).addListener(this.forSkinIdCacheOnly(profileId));
    }
    if (profile.getProperties().containsKey("textures")) {
      return this.mojangProfileCache.getUnchecked(profile);
    } else {
      return ImmutableMap.of();
    }
  }

  private void handleRemove(RemovalNotification<Object, ?> notification) {
    if (notification.getKey() instanceof UUID && HANDLED_CAUSES.contains(notification.getCause())) {
      if (this.addonContext.getActive().get()) {
        this.sentAllQueuedUnloads();
        this.addonContext.getNetworkClient().sendPacket(new PacketClientLiveSkinUnload((UUID) notification.getKey()));
      } else {
        this.nonSentUnloads.add((UUID) notification.getKey());
      }
    }
  }

  private void sentAllQueuedUnloads() {
    if (!this.nonSentUnloads.isEmpty()) {
      UUID uniqueId;
      while ((uniqueId = this.nonSentUnloads.poll()) != null) {
        if (this.uniqueIdToSkinHashCache.getIfPresent(uniqueId) == null) {
          this.addonContext.getNetworkClient().sendPacket(new PacketClientLiveSkinUnload(uniqueId));
        }
      }
    }
  }

  @Nonnull
  private FutureListener<PacketBase> newListenerForSkinIdLoad(UUID uniqueId, GameProfile profile, @Nullable SkinAvailableCallback callback, boolean requireSecure) {
    return FunctionalFutureListener.listener(packetBase -> {
      if (packetBase instanceof PacketServerResponseSkinId) {
        PacketServerResponseSkinId response = (PacketServerResponseSkinId) packetBase;
        if (response.hasSkin()) {
          HDSkinManager.this.uniqueIdToSkinHashCache.put(uniqueId, SkinHashWrapper.wrap(response));
          HDSkinManager.this.loadSkin(HDMinecraftProfileTexture.texture(response), MinecraftProfileTexture.Type.SKIN, callback);
        } else {
          HDSkinManager.this.uniqueIdToSkinHashCache.put(uniqueId, NO_SKIN);
          HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
        }
      }
    }, () -> {
      HDSkinManager.this.uniqueIdToSkinHashCache.put(uniqueId, NO_SKIN);
      HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
    });
  }

  @Nonnull
  private FutureListener<PacketBase> forSkinIdCacheOnly(UUID profileId) {
    return FunctionalFutureListener.listener(packetBase -> {
      if (packetBase instanceof PacketServerResponseSkinId) {
        PacketServerResponseSkinId response = (PacketServerResponseSkinId) packetBase;
        if (response.hasSkin()) {
          HDSkinManager.this.uniqueIdToSkinHashCache.put(profileId, SkinHashWrapper.wrap(response));
        } else {
          HDSkinManager.this.uniqueIdToSkinHashCache.put(profileId, NO_SKIN);
        }
      }
    }, () -> HDSkinManager.this.uniqueIdToSkinHashCache.put(profileId, NO_SKIN));
  }

  @Nonnull
  private FutureListener<PacketBase> forSkinLoad(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type,
                                                 @Nullable SkinAvailableCallback callback, HDResourceLocation location, Path targetLocalPath) {
    return FunctionalFutureListener.listener(new SkinLoadPacketHandler(
      targetLocalPath,
      location,
      this.textureManager,
      () -> HDSkinManager.super.loadSkin(texture, type, callback),
      texture,
      type,
      callback
    ), () -> MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback))));
  }

  public void pushSkinDelete(String skinHash) {
    for (Map.Entry<MinecraftProfileTexture, HDResourceLocation> entry : this.textureToLocationCache.asMap().entrySet()) {
      if (entry.getKey().getHash().equals(skinHash)) {
        this.textureToLocationCache.invalidate(entry.getKey());
        break;
      }
    }

    for (Map.Entry<UUID, SkinHashWrapper> entry : this.uniqueIdToSkinHashCache.asMap().entrySet()) {
      if (entry.getValue().hasSkin() && entry.getValue().getSkinHash().equals(skinHash)) {
        this.updateSkin(entry.getKey(), NO_SKIN);
      }
    }
  }

  public void pushSkinDelete(UUID playerUniqueId) {
    this.updateSkin(playerUniqueId, NO_SKIN);
  }

  public void pushMaxResolutionUpdate() {
    for (UUID uuid : this.uniqueIdToSkinHashCache.asMap().keySet()) {
      this.updateSkin(uuid, null);
    }
  }

  public void pushSkinUpdate(UUID playerUniqueId, String newSkinHash) {
    SkinHashWrapper wrapper = this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId);
    if (wrapper != null) {
      if (wrapper == NO_SKIN) {
        wrapper = SkinHashWrapper.wrap(newSkinHash);
      } else {
        wrapper.setSkinHash(newSkinHash);
      }
    }

    this.updateSkin(playerUniqueId, wrapper);
  }

  public void pushSkinSlimChange(UUID playerUniqueId, boolean newSlimState) {
    SkinHashWrapper wrapper = this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId);
    if (wrapper != null && wrapper != NO_SKIN) {
      wrapper.setSlim(newSlimState);
    }

    this.updateSkin(playerUniqueId, wrapper);
  }

  private void updateSkin(UUID uniqueId, @Nullable SkinHashWrapper wrapper) {
    if (wrapper != null && wrapper != NO_SKIN) {
      for (Map.Entry<MinecraftProfileTexture, HDResourceLocation> entry : this.textureToLocationCache.asMap().entrySet()) {
        if (entry.getKey().getHash().equals(wrapper.getSkinHash())) {
          this.textureToLocationCache.invalidate(entry.getKey());
          break;
        }
      }
    }

    if (wrapper != null) {
      this.uniqueIdToSkinHashCache.put(uniqueId, wrapper);
    } else {
      this.uniqueIdToSkinHashCache.invalidate(uniqueId);
    }
    this.skinInvalidator.accept(uniqueId);
  }

  public AddonContext getAddonContext() {
    return this.addonContext;
  }
}
