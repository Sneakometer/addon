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
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import de.hdskins.labymod.shared.MCUtil;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.concurrent.ConcurrentUtil;
import de.hdskins.labymod.shared.listener.ClientListeners;
import de.hdskins.labymod.shared.listener.NetworkListeners;
import de.hdskins.labymod.shared.resource.HDResourceLocation;
import de.hdskins.labymod.shared.utils.Constants;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.download.PacketClientRequestSkin;
import de.hdskins.protocol.packets.reading.download.PacketClientRequestSkinId;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkin;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkinId;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveSkinUnload;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    private static final SkinHashWrapper NO_SKIN = new SkinHashWrapper();
    private static final Logger LOGGER = LogManager.getLogger(HDSkinManager.class);
    private static final Map<String, String> SLIM = ImmutableMap.of("model", "slim");
    private static final Collection<RemovalCause> HANDLED_CAUSES = EnumSet.of(RemovalCause.SIZE, RemovalCause.COLLECTED, RemovalCause.EXPIRED);

    private final Path assetsDirectory;
    private final AddonContext addonContext;
    private final Consumer<UUID> skinLoadedRemover;
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

    public HDSkinManager(AddonContext addonContext, File mcAssetsDir, Consumer<UUID> skinLoadedRemover) {
        super(Minecraft.getMinecraft().getTextureManager(), new File(mcAssetsDir, "skins"), Minecraft.getMinecraft().getSessionService());
        this.assetsDirectory = mcAssetsDir.toPath();
        this.addonContext = addonContext;
        this.skinLoadedRemover = skinLoadedRemover;
        // Register listeners to this skin manager
        addonContext.getNetworkClient().getPacketListenerRegistry().registerListeners(new NetworkListeners(this));
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
        Path localSkinPath = this.assetsDirectory.resolve(location.getPath());
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
            this.loadSkin(new HDMinecraftProfileTexture(response.getSkinHash(), response.isSlim() ? SLIM : null), MinecraftProfileTexture.Type.SKIN, callback);
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
        if (response != null) {
            if (!response.hasSkin()) {
                return this.mojangProfileCache.getUnchecked(profile);
            }
            return ImmutableMap.of(MinecraftProfileTexture.Type.SKIN, new HDMinecraftProfileTexture(response.getSkinHash(), response.isSlim() ? SLIM : null));
        }
        if (this.addonContext.getActive().get()) {
            this.addonContext.getNetworkClient().sendQuery(new PacketClientRequestSkinId(profile.getId())).addListener(this.forSkinIdCacheOnly(profile));
        }
        return this.mojangProfileCache.getUnchecked(profile);
    }

    private void handleRemove(RemovalNotification<Object, ?> notification) {
        if (notification.getKey() instanceof UUID && HANDLED_CAUSES.contains(notification.getCause())) {
            if (this.addonContext.getActive().get()) {
                this.sentAllQueuedUnloads();
                this.addonContext.getNetworkClient().sendPacket(new PacketServerLiveSkinUnload((UUID) notification.getKey()));
            } else {
                this.nonSentUnloads.add((UUID) notification.getKey());
            }
        }
    }

    private void sentAllQueuedUnloads() {
        if (!this.nonSentUnloads.isEmpty()) {
            UUID uniqueId;
            while ((uniqueId = this.nonSentUnloads.poll()) != null) {
                this.addonContext.getNetworkClient().sendPacket(new PacketServerLiveSkinUnload(uniqueId));
            }
        }
    }

    private FutureListener<PacketBase> forSkinIdLoad(GameProfile profile, @Nullable SkinAvailableCallback callback, boolean requireSecure) {
        return new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
            }

            @Override
            public void nonNullResult(@Nonnull PacketBase packetBase) {
                if (packetBase instanceof PacketServerResponseSkinId) {
                    PacketServerResponseSkinId response = (PacketServerResponseSkinId) packetBase;
                    if (response.hasSkin()) {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), new SkinHashWrapper(response));
                        HDSkinManager.this.loadSkin(new HDMinecraftProfileTexture(response.getSkinId(), response.isSlim() ? SLIM : null), MinecraftProfileTexture.Type.SKIN, callback);
                    } else {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                        HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
                    }
                }
            }

            @Override
            public void cancelled() {
                HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
            }
        };
    }

    private FutureListener<PacketBase> forSkinIdCacheOnly(GameProfile profile) {
        return new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
            }

            @Override
            public void nonNullResult(PacketBase packetBase) {
                if (packetBase instanceof PacketServerResponseSkinId) {
                    PacketServerResponseSkinId response = (PacketServerResponseSkinId) packetBase;
                    if (response.hasSkin()) {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), new SkinHashWrapper(response));
                    } else {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
                    }
                }
            }

            @Override
            public void cancelled() {
                HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), NO_SKIN);
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

                    final Path parent = targetLocalPath.getParent();
                    if (parent != null && Files.notExists(parent)) {
                        try {
                            Files.createDirectories(parent);
                        } catch (IOException exception) {
                            LOGGER.debug("Unable to create directory {} for skin download to {}", parent, targetLocalPath, exception);
                            MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
                            return;
                        }
                    }

                    IImageBuffer buffer = new ImageBufferDownload();
                    try (InputStream stream = new ByteArrayInputStream(response.getSkinData())) {
                        BufferedImage bufferedImage = buffer.parseUserSkin(ImageIO.read(stream));
                        try (OutputStream outputStream = Files.newOutputStream(targetLocalPath, StandardOpenOption.CREATE)) {
                            ImageIO.write(bufferedImage, "png", outputStream);
                        }

                        MCUtil.call(ConcurrentUtil.fromRunnable(() -> {
                            HDSkinManager.this.textureManager.loadTexture(location, new HDSkinTexture(bufferedImage));
                            if (callback != null) {
                                callback.skinAvailable(type, location, texture);
                            }
                        }));
                    } catch (IOException exception) {
                        LOGGER.debug("Unable to load skin of texture: {} type: {} callback: {} path: {}", texture, type, callback, targetLocalPath, exception);
                        MCUtil.call(ConcurrentUtil.fromRunnable(() -> HDSkinManager.super.loadSkin(texture, type, callback)));
                    }
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
        for (Map.Entry<MinecraftProfileTexture, HDResourceLocation> entry : this.textureToLocationCache.asMap().entrySet()) {
            if (entry.getKey().getHash().equals(skinHash)) {
                this.textureToLocationCache.invalidate(entry.getKey());
                break;
            }
        }

        for (Map.Entry<UUID, SkinHashWrapper> entry : this.uniqueIdToSkinHashCache.asMap().entrySet()) {
            if (entry.getValue().hasSkin() && entry.getValue().getSkinHash().equals(skinHash)) {
                this.uniqueIdToSkinHashCache.put(entry.getKey(), NO_SKIN);
                this.skinLoadedRemover.accept(entry.getKey());
            }
        }
    }

    public void pushSkinDelete(UUID playerUniqueId) {
        if (this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId) != null) {
            this.uniqueIdToSkinHashCache.put(playerUniqueId, NO_SKIN);
        }

        this.skinLoadedRemover.accept(playerUniqueId);
    }

    public void pushMaxResolutionUpdate() {
        for (UUID uuid : this.uniqueIdToSkinHashCache.asMap().keySet()) {
            this.uniqueIdToSkinHashCache.invalidate(uuid);
            this.skinLoadedRemover.accept(uuid);
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

        this.skinLoadedRemover.accept(playerUniqueId);
    }

    public void pushSkinSlimChange(UUID playerUniqueId, boolean newSlimState) {
        SkinHashWrapper wrapper = this.uniqueIdToSkinHashCache.getIfPresent(playerUniqueId);
        if (wrapper != null && wrapper != NO_SKIN) {
            wrapper.setSlim(newSlimState);
        }

        this.skinLoadedRemover.accept(playerUniqueId);
    }

    private static final class HDMinecraftProfileTexture extends MinecraftProfileTexture {

        public HDMinecraftProfileTexture(String hash, @Nullable Map<String, String> metadata) {
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

            if (!(obj instanceof HDMinecraftProfileTexture)) {
                return false;
            }

            return ((HDMinecraftProfileTexture) obj).getHash().equals(this.getHash());
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

        public void setSkinHash(@Nullable String skinHash) {
            this.skinHash = skinHash;
        }

        public boolean isSlim() {
            return this.slim;
        }

        public void setSlim(boolean slim) {
            this.slim = slim;
        }
    }
}
