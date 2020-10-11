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
import de.hdskins.labymod.shared.listener.NetworkConnectionInactiveListener;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
public class HDSkinManager extends SkinManager {

    private static final Logger LOGGER = LogManager.getLogger(HDSkinManager.class);
    private static final Map<String, String> SLIM = Collections.singletonMap("model", "slim");

    private final Path skinCacheDirectory;
    private final AddonContext addonContext;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    private final LoadingCache<MinecraftProfileTexture, HDResourceLocation> textureToLocationCache = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .concurrencyLevel(4)
        .ticker(Ticker.systemTicker())
        .build(new CacheLoader<MinecraftProfileTexture, HDResourceLocation>() {
            @Override
            public HDResourceLocation load(MinecraftProfileTexture texture) {
                return HDResourceLocation.forProfileTexture(texture);
            }
        });
    private final Cache<UUID, PacketServerResponseSkinId> uniqueIdToSkinHashCache = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .concurrencyLevel(4)
        .ticker(Ticker.systemTicker())
        .build();

    public HDSkinManager(AddonContext addonContext, Path mcSkinsDir) {
        super(Minecraft.getMinecraft().getTextureManager(), mcSkinsDir.toFile(), Minecraft.getMinecraft().getSessionService());
        this.skinCacheDirectory = mcSkinsDir;
        this.addonContext = addonContext;
        addonContext.getNetworkClient().getPacketListenerRegistry().registerListeners(new NetworkConnectionInactiveListener(this.active));
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type) {
        return this.loadSkin(texture, type, null);
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type, SkinAvailableCallback callback) {
        // Build a resource location for the profile texture
        HDResourceLocation location = this.textureToLocationCache.getUnchecked(texture);
        // Test if the texture is already loaded and cached
        ITextureObject textureObject = this.textureManager.getTexture(location);
        // If the texture object is non-null we can break now
        if (textureObject != null) {
            // If a callback is provided by the method we should post the result to it
            if (callback != null) {
                callback.skinAvailable(type, location, texture);
            }
            return location;
        }
        // Check if the file locally already exits
        Path localSkinPath = this.skinCacheDirectory.resolve(location.getResourcePath());
        if (Files.exists(localSkinPath) && this.textureManager.loadTexture(location, new HDSkinTexture(location))) {
            // If a callback is provided by the method we should post the result to it
            if (callback != null) {
                callback.skinAvailable(type, location, texture);
            }
            return location;
        }
        // We were disconnected from the server so we cannot load a skin from there
        if (!this.active.get()) {
            // If we are not reconnecting already, begin now
            if (!this.reconnecting.getAndSet(true)) {
                BackendUtils.reconnect(this.addonContext).thenRun(() -> {
                    this.active.set(true);
                    this.reconnecting.set(false);
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

        PacketServerResponseSkinId response = this.uniqueIdToSkinHashCache.getIfPresent(profile.getId());
        if (response == null) {
            // We were disconnected from the server so we cannot load a skin from there
            if (!this.active.get()) {
                // If we are not reconnecting already, begin now
                if (!this.reconnecting.getAndSet(true)) {
                    BackendUtils.reconnect(this.addonContext).thenRun(() -> {
                        this.active.set(true);
                        this.reconnecting.set(false);
                    });
                }
                // As we are not connected to the server we try a legacy lookup
                super.loadProfileTextures(profile, callback, requireSecure);
                return;
            }
            // We send a query to the server to get the skin hash from the uuid given
            this.addonContext.getNetworkClient().sendQuery(new PacketClientRequestSkinId(profile.getId())).addListener(this.forSkinIdLoad(profile, callback, requireSecure));
        } else {
            // We already sent a request to the server so we can now simply load the texture by the hash we already got from the server
            this.loadSkin(new SimpleMinecraftProfileTexture(response.getSkinId(), response.isSlim() ? SLIM : null), MinecraftProfileTexture.Type.SKIN, callback);
        }
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        if (profile.getId() == null) {
            LOGGER.debug("Unable to load skin {} from cache because skin unique id is null", profile);
            return Collections.emptyMap();
        }

        PacketServerResponseSkinId response = this.uniqueIdToSkinHashCache.getIfPresent(profile.getId());
        if (response == null) {
            // TODO: we need a workaround for loading the skin data here (used to render skulls)
            return Collections.emptyMap();
        }

        if (!response.hasSkin()) {
            // TODO: we have to make a cache for id -> mojang fetched profile
            return Collections.emptyMap();
        }

        return Collections.singletonMap(MinecraftProfileTexture.Type.SKIN, new SimpleMinecraftProfileTexture(response.getSkinId(), response.isSlim() ? SLIM : null));
    }

    private FutureListener<PacketBase> forSkinIdLoad(GameProfile profile, SkinAvailableCallback callback, boolean requireSecure) {
        return new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
            }

            @Override
            public void nonNullResult(PacketBase packetBase) {
                if (packetBase instanceof PacketServerResponseSkinId) {
                    PacketServerResponseSkinId response = (PacketServerResponseSkinId) packetBase;
                    if (response.hasSkin()) {
                        HDSkinManager.this.uniqueIdToSkinHashCache.put(profile.getId(), response);
                        HDSkinManager.this.loadSkin(new SimpleMinecraftProfileTexture(response.getSkinId(), response.isSlim() ? SLIM : null), MinecraftProfileTexture.Type.SKIN, callback);
                    } else {
                        HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
                    }
                }
            }

            @Override
            public void cancelled() {
                HDSkinManager.super.loadProfileTextures(profile, callback, requireSecure);
            }
        };
    }

    private FutureListener<PacketBase> forSkinLoad(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type, SkinAvailableCallback callback, HDResourceLocation location, Path targetLocalPath) {
        return new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                HDSkinManager.super.loadSkin(texture, type, callback);
            }

            @Override
            public void nonNullResult(PacketBase packetBase) {
                if (packetBase instanceof PacketServerResponseSkin) {
                    PacketServerResponseSkin response = (PacketServerResponseSkin) packetBase;
                    IImageBuffer buffer = new ImageBufferDownload();
                    try (InputStream stream = new ByteArrayInputStream(response.getSkinData())) {
                        BufferedImage bufferedImage = buffer.parseUserSkin(ImageIO.read(stream));
                        try (OutputStream outputStream = Files.newOutputStream(targetLocalPath)) {
                            ImageIO.write(bufferedImage, "png", outputStream);
                        }
                    } catch (IOException exception) {
                        LOGGER.debug("Unable to load skin of texture: {} type: {} callback: {} path: {}", texture, type, callback, targetLocalPath, exception);
                        HDSkinManager.super.loadSkin(texture, type, callback);
                    }

                    HDSkinManager.this.textureManager.loadTexture(location, new HDSkinTexture(location));
                    if (callback != null) {
                        MCUtil.call(ConcurrentUtil.fromRunnable(() -> callback.skinAvailable(type, location, texture)));
                    }
                } else {
                    HDSkinManager.super.loadSkin(texture, type, callback);
                }
            }

            @Override
            public void cancelled() {
                HDSkinManager.super.loadSkin(texture, type, callback);
            }
        };
    }

    private static final class SimpleMinecraftProfileTexture extends MinecraftProfileTexture {

        public SimpleMinecraftProfileTexture(String hash, Map<String, String> metadata) {
            super(hash, metadata);
        }

        @Override
        public String getHash() {
            return super.getUrl(); // Easy workaround :)
        }
    }
}
