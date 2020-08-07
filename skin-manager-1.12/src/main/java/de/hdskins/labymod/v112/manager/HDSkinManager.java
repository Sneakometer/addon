package de.hdskins.labymod.v112.manager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.v112.resources.InternalMinecraftProfileTexture;
import de.hdskins.labymod.v112.resources.InternalThreadDownloadImageData;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class HDSkinManager extends SkinManager {

    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private final File skinCacheDir;
    private final TextureManager textureManager;
    private final ConfigObject configObject;

    private final Map<UUID, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cache = new ConcurrentHashMap<>();

    public HDSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService, ConfigObject configObject) {
        super(textureManagerInstance, skinCacheDirectory, sessionService);
        this.skinCacheDir = skinCacheDirectory;
        this.textureManager = textureManagerInstance;
        this.configObject = configObject;
    }

    @Override
    public void loadProfileTextures(GameProfile profile, SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        loadProfileTextures0(profile, skinAvailableCallback, requireSecure, false);
    }

    private void loadProfileTextures0(GameProfile profile, SkinAvailableCallback skinAvailableCallback, boolean requireSecure, boolean fillProperties) {
        THREAD_POOL.execute(() -> {
            CompletableFuture<Boolean> future = null;
            InternalMinecraftProfileTexture texture = null;
            if (this.configObject.getServerUrl() != null && !this.configObject.getServerUrl().trim().isEmpty()) {
                future = new CompletableFuture<>();
                String undashed = profile.getId().toString().replace("-", "");

                texture = new InternalMinecraftProfileTexture(this.configObject.getServerUrl() + "/downloadSkin?uuid=" + undashed, undashed);
                try {
                    this.loadSkin0(texture, MinecraftProfileTexture.Type.SKIN, skinAvailableCallback, future);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();
            if (fillProperties && Iterables.getFirst(profile.getProperties().get("textures"), null) == null
                    && !profile.getId().equals(LabyMod.getInstance().getPlayerUUID())) {
                Minecraft.getMinecraft().getSessionService().fillProfileProperties(profile, false);
            }

            try {
                map.putAll(Minecraft.getMinecraft().getSessionService().getTextures(profile, requireSecure));
            } catch (InsecureTextureException ignored) {
            }

            if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
                profile.getProperties().clear();
                profile.getProperties().putAll(Minecraft.getMinecraft().getProfileProperties());
                map.putAll(Minecraft.getMinecraft().getSessionService().getTextures(profile, false));
            }

            this.postLoad(map, future, texture, skinAvailableCallback, profile);

            if (future != null && !future.isDone()) {
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (InterruptedException | TimeoutException | ExecutionException exception) {
                    future.complete(false);
                }
            }
        });
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        if (profile.getId() == null) {
            return Collections.emptyMap();
        }

        if (this.cache.containsKey(profile.getId())) {
            return this.cache.get(profile.getId());
        }

        try {
            this.cache.put(profile.getId(), Minecraft.getMinecraft().getSessionService().getTextures(profile, false));
        } catch (Throwable ignored) {
        }

        this.loadProfileTextures0(profile, null, false, true);
        return this.cache.getOrDefault(profile.getId(), Collections.emptyMap());
    }

    public void loadSkin0(MinecraftProfileTexture profileTexture,
                          MinecraftProfileTexture.Type type,
                          SkinAvailableCallback skinAvailableCallback,
                          CompletableFuture<Boolean> future) {
        final ResourceLocation resourcelocation = new ResourceLocation("skins/" + profileTexture.getHash());
        ITextureObject iTextureObject = this.textureManager.getTexture(resourcelocation);

        boolean internal = profileTexture instanceof InternalMinecraftProfileTexture && iTextureObject instanceof InternalThreadDownloadImageData;
        boolean canUseCachedData = internal && ((InternalThreadDownloadImageData) iTextureObject).getBufferedImage() != null;

        if ((internal && canUseCachedData) || (!internal && iTextureObject != null)) {
            if (skinAvailableCallback != null) {
                skinAvailableCallback.skinAvailable(type, resourcelocation, profileTexture);
            }

            if (future != null) {
                future.complete(true);
            }
        } else {
            File file1 = new File(this.skinCacheDir, profileTexture.getHash().length() > 2 ? profileTexture.getHash().substring(0, 2) : "xx");
            File file2 = new File(file1, profileTexture.getHash());
            final IImageBuffer iimagebuffer = type == MinecraftProfileTexture.Type.SKIN ? new ImageBufferDownload() : null;
            SimpleTexture texture = new InternalThreadDownloadImageData(file2, profileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer() {
                @Override
                public BufferedImage parseUserSkin(BufferedImage image) {
                    if (iimagebuffer != null) {
                        image = iimagebuffer.parseUserSkin(image);
                    }

                    return image;
                }

                @Override
                public void skinAvailable() {
                    if (iimagebuffer != null) {
                        iimagebuffer.skinAvailable();
                    }

                    if (skinAvailableCallback != null) {
                        skinAvailableCallback.skinAvailable(type, resourcelocation, profileTexture);
                    }
                }
            }, future, profileTexture instanceof InternalMinecraftProfileTexture ? (InternalMinecraftProfileTexture) profileTexture : null);
            Minecraft.getMinecraft().addScheduledTask(() -> HDSkinManager.this.textureManager.loadTexture(resourcelocation, texture));
        }
    }

    private void postLoad(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map, CompletableFuture<Boolean> future,
                          InternalMinecraftProfileTexture texture, SkinAvailableCallback skinAvailableCallback, GameProfile profile) {
        try {
            if (future != null) {
                future.thenAccept(success -> {
                    if (success && texture != null) {
                        map.put(MinecraftProfileTexture.Type.SKIN, texture);
                    } else if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                        this.loadSkin0(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, skinAvailableCallback, null);
                    }
                });
            } else {
                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    this.loadSkin0(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, skinAvailableCallback, null);
                }
            }

            if (map.containsKey(MinecraftProfileTexture.Type.CAPE)) {
                this.loadSkin0(map.get(MinecraftProfileTexture.Type.CAPE), MinecraftProfileTexture.Type.CAPE, skinAvailableCallback, null);
            }

            this.cache.put(profile.getId(), map);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public ResourceLocation loadSkinLocation(UUID uniqueId) {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textureMap = this.loadSkinFromCache(new GameProfile(uniqueId, "Steve"));
        if (textureMap == null) {
            return null;
        }

        MinecraftProfileTexture texture = textureMap.get(MinecraftProfileTexture.Type.SKIN);
        return texture == null ? null : this.loadSkin(texture, MinecraftProfileTexture.Type.SKIN);
    }
}