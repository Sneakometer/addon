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
package de.hdskins.labymod.shared.texture;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import de.hdskins.labymod.shared.MCUtil;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.concurrent.ConcurrentUtil;
import de.hdskins.labymod.shared.listener.ClientListeners;
import de.hdskins.labymod.shared.listener.NetworkListeners;
import de.hdskins.labymod.shared.resource.HDResourceLocation;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.download.PacketClientRequestSkin;
import de.hdskins.protocol.packets.reading.download.PacketClientRequestSkinId;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkin;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkinId;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ParametersAreNonnullByDefault
@SuppressWarnings("UnstableApiUsage")
public class HDSkinManager extends SkinManager {

    private static final SkinHashWrapper NO_SKIN = new SkinHashWrapper();
    private static final Logger LOGGER = LogManager.getLogger(HDSkinManager.class);
    private static final Map<String, String> SLIM = Collections.singletonMap("model", "slim");

    private final Path skinCacheDirectory;
    private final AddonContext addonContext;
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
        .concurrencyLevel(4)
        .ticker(Ticker.systemTicker())
        .build();

    public HDSkinManager(AddonContext addonContext, Path mcSkinsDir) {
        super(Minecraft.getMinecraft().getTextureManager(), mcSkinsDir.toFile(), Minecraft.getMinecraft().getSessionService());
        this.skinCacheDirectory = mcSkinsDir;
        this.addonContext = addonContext;
        // Register listeners to this skin manager
        addonContext.getNetworkClient().getPacketListenerRegistry().registerListeners(new NetworkListeners(this));
        MinecraftForge.EVENT_BUS.register(new ClientListeners(this));
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type) {
        return this.loadSkin(texture, type, null);
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type, @Nullable SkinAvailableCallback callback) {
        // Build a resource location for the profile texture
        HDResourceLocation location = this.textureToLocationCache.getUnchecked(texture);
        // Test if the texture is already loaded and cached
        ITextureObject textureObject = this.textureManager.getTexture(location);
        // If the texture object is non-null we can break now
        if (textureObject != null) {
            // If a callback is provided by the method we should post the result to it
            if (callback != null) {
                MCUtil.call(ConcurrentUtil.fromRunnable(() -> callback.skinAvailable(type, location, texture)));
            }
            return location;
        }
        // Check if the file locally already exits
        Path localSkinPath = this.skinCacheDirectory.resolve(location.getResourcePath());
        if (Files.exists(localSkinPath) && this.textureManager.loadTexture(location, new HDSkinTexture(location))) {
            // If a callback is provided by the method we should post the result to it
            if (callback != null) {
                MCUtil.call(ConcurrentUtil.fromRunnable(() -> callback.skinAvailable(type, location, texture)));
            }
            return location;
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
        if ((!profile.getId().equals(self) && !this.addonContext.getAddonConfig().showSkinsOfOtherPlayers()) || this.addonContext.getAddonConfig().isSkinDisabled(profile.getId())) {
            LOGGER.debug("Not loading skin for profile: {} callback: {} secure: {} because the unique id is blocked locally.", profile, callback, requireSecure);
            super.loadProfileTextures(profile, callback, requireSecure);
            return;
        }

        SkinHashWrapper response = this.uniqueIdToSkinHashCache.getIfPresent(profile.getId());
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
            this.addonContext.getNetworkClient().sendQuery(new PacketClientRequestSkinId(profile.getId())).addListener(this.forSkinIdLoad(profile, callback, requireSecure));
        } else if (response.hasSkin()) {
            // We already sent a request to the server so we can now simply load the texture by the hash we already got from the server
            this.loadSkin(new SimpleMinecraftProfileTexture(response.getSkinHash(), response.isSlim() ? SLIM : null), MinecraftProfileTexture.Type.SKIN, callback);
        } else {
            // The player has no hd skin so we do a legacy load
            super.loadProfileTextures(profile, callback, requireSecure);
        }
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        if (profile.getId() == null) {
            LOGGER.debug("Unable to load skin {} from cache because skin unique id is null", profile);
            return this.mojangProfileCache.getUnchecked(profile);
        }

        SkinHashWrapper response = this.uniqueIdToSkinHashCache.getIfPresent(profile.getId());
        if (response == null) {
            try {
                PacketBase packetBase = this.addonContext.getNetworkClient().sendQuery(new PacketClientRequestSkinId(profile.getId())).get(3, TimeUnit.SECONDS);
                if (packetBase instanceof PacketServerResponseSkinId) {
                    response = new SkinHashWrapper((PacketServerResponseSkinId) packetBase);
                } else {
                    response = NO_SKIN;
                }
            } catch (ExecutionException | TimeoutException exception) {
                LOGGER.debug("Unable to load skin {} from cache because the server answered too lazy", profile, exception);
                response = NO_SKIN;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return Collections.emptyMap(); // unreachable code
            }
            // We cache the result as we have a listener which informs us about changes
            // we don't need to worry about that the user might has changed the skin.
            this.uniqueIdToSkinHashCache.put(profile.getId(), response);
        }

        if (!response.hasSkin()) {
            LOGGER.debug("Loading skin of {} using the session service because server told us that the user has no skin", profile);
            return this.mojangProfileCache.getUnchecked(profile);
        }

        LOGGER.debug("Loaded skin of user {} successfully! Hash: {} Slim: {}", profile, response.getSkinHash(), response.isSlim());
        return Collections.singletonMap(MinecraftProfileTexture.Type.SKIN, new SimpleMinecraftProfileTexture(response.getSkinHash(), response.isSlim() ? SLIM : null));
    }

    private FutureListener<PacketBase> forSkinIdLoad(GameProfile profile, SkinAvailableCallback callback, boolean requireSecure) {
        return new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure)));
            }

            @Override
            public void nonNullResult(@Nonnull PacketBase packetBase) {
                if (packetBase instanceof PacketServerResponseSkinId) {
                    PacketServerResponseSkinId response = (PacketServerResponseSkinId) packetBase;
                    if (response.hasSkin()) {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), new SkinHashWrapper(response));
                        HDSkinManager.this.loadSkin(new SimpleMinecraftProfileTexture(response.getSkinId(), response.isSlim() ? SLIM : null), MinecraftProfileTexture.Type.SKIN, callback);
                    } else {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                        MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure)));
                    }
                }
            }

            @Override
            public void cancelled() {
                HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure)));
            }
        };
    }

    private FutureListener<PacketBase> forSkinLoad(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type,
                                                   @Nullable SkinAvailableCallback callback, HDResourceLocation location, Path targetLocalPath) {
        return new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
            }

            @Override
            public void nonNullResult(@Nonnull PacketBase packetBase) {
                if (packetBase instanceof PacketServerResponseSkin) {
                    PacketServerResponseSkin response = (PacketServerResponseSkin) packetBase;
                    if (response.getSkinData().length == 0) {
                        LOGGER.debug("Unable to load skin with hash {} because server sent an empty result", texture.getHash());
                        MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
                        return;
                    }

                    IImageBuffer buffer = new ImageBufferDownload();
                    try (InputStream stream = new ByteArrayInputStream(response.getSkinData())) {
                        BufferedImage bufferedImage = buffer.parseUserSkin(ImageIO.read(stream));
                        try (OutputStream outputStream = Files.newOutputStream(targetLocalPath)) {
                            ImageIO.write(bufferedImage, "png", outputStream);
                        }
                    } catch (IOException exception) {
                        LOGGER.debug("Unable to load skin of texture: {} type: {} callback: {} path: {}", texture, type, callback, targetLocalPath, exception);
                        MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
                    }

                    MCUtil.call(ConcurrentUtil.fromRunnable(() -> {
                        HDSkinManager.this.textureManager.loadTexture(location, new HDSkinTexture(location));
                        if (callback != null) {
                            callback.skinAvailable(type, location, texture);
                        }
                    }));
                } else {
                    MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
                }
            }

            @Override
            public void cancelled() {
                MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
            }
        };
    }

    public AddonContext getAddonContext() {
        return this.addonContext;
    }

    public void pushSkinDelete(String skinHash) {
        for (Map.Entry<UUID, SkinHashWrapper> entry : this.uniqueIdToSkinHashCache.asMap().entrySet()) {
            if (entry.getValue().hasSkin() && entry.getValue().getSkinHash().equals(skinHash)) {
                this.uniqueIdToSkinHashCache.put(entry.getKey(), NO_SKIN);
            }
        }

        for (Map.Entry<MinecraftProfileTexture, HDResourceLocation> entry : this.textureToLocationCache.asMap().entrySet()) {
            if (entry.getKey().getHash().equals(skinHash)) {
                this.textureToLocationCache.invalidate(entry.getKey());
                break;
            }
        }
    }

    public void pushSkinDelete(UUID playerUniqueId) {
        if (this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId) != null) {
            this.uniqueIdToSkinHashCache.put(playerUniqueId, NO_SKIN);
        }
    }

    public void pushSkinUpdate(UUID playerUniqueId, String newSkinHash) {
        SkinHashWrapper wrapper = this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId);
        if (wrapper != null) {
            if (wrapper == NO_SKIN) {
                this.uniqueIdToSkinHashCache.put(playerUniqueId, new SkinHashWrapper(newSkinHash));
            } else {
                wrapper.setSkinHash(newSkinHash);
            }
        }
    }

    public void pushSkinSlimChange(UUID playerUniqueId, boolean newSlimState) {
        SkinHashWrapper wrapper = this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId);
        if (wrapper != null && wrapper != NO_SKIN) {
            wrapper.setSlim(newSlimState);
        }
    }

    private static final class SimpleMinecraftProfileTexture extends MinecraftProfileTexture {

        public SimpleMinecraftProfileTexture(String hash, @Nullable Map<String, String> metadata) {
            super(hash, metadata);
        }

        @Override
        public String getHash() {
            return super.getUrl(); // Easy workaround :)
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof SimpleMinecraftProfileTexture)) {
                return false;
            }

            return ((SimpleMinecraftProfileTexture) obj).getHash().equals(this.getHash());
        }

        @Override
        public int hashCode() {
            return this.getHash().hashCode();
        }
    }

    private static final class SkinHashWrapper {

        private String skinHash;
        private boolean slim;

        public SkinHashWrapper() {
        }

        public SkinHashWrapper(String skinHash) {
            this.skinHash = skinHash;
        }

        public SkinHashWrapper(PacketServerResponseSkinId responseSkinId) {
            this.skinHash = responseSkinId.getSkinId();
            this.slim = responseSkinId.isSlim();
        }

        public boolean hasSkin() {
            return this.skinHash != null;
        }

        public String getSkinHash() {
            return this.skinHash;
        }

        public boolean isSlim() {
            return this.slim;
        }

        public void setSkinHash(@Nullable String skinHash) {
            this.skinHash = skinHash;
        }

        public void setSlim(boolean slim) {
            this.slim = slim;
        }
    }
}
