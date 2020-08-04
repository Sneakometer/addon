package de.hdskins.labymod.v18.manager;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.v18.resources.InternalMinecraftProfileTexture;
import de.hdskins.labymod.v18.resources.InternalThreadDownloadImageData;
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
import java.util.Map;
import java.util.concurrent.*;

public class HDSkinManager extends SkinManager {

    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private final File skinCacheDir;
    private final TextureManager textureManager;
    private final ConfigObject configObject;

    public HDSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService, ConfigObject configObject) {
        super(textureManagerInstance, skinCacheDirectory, sessionService);
        this.skinCacheDir = skinCacheDirectory;
        this.textureManager = textureManagerInstance;
        this.configObject = configObject;
    }

    @Override
    public void loadProfileTextures(GameProfile profile, SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        THREAD_POOL.execute(() -> {
            CompletableFuture<Boolean> future = null;
            if (this.configObject.getServerUrl() != null && !this.configObject.getServerUrl().trim().isEmpty()) {
                future = new CompletableFuture<>();
                String undashed = profile.getId().toString().replace("-", "");

                MinecraftProfileTexture texture = new InternalMinecraftProfileTexture(this.configObject.getServerUrl() + "/downloadSkin?uuid=" + undashed, undashed);
                try {
                    this.loadSkin0(texture, MinecraftProfileTexture.Type.SKIN, skinAvailableCallback, future);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();
            try {
                map.putAll(Minecraft.getMinecraft().getSessionService().getTextures(profile, requireSecure));
            } catch (InsecureTextureException ignored) {
            }

            if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
                profile.getProperties().clear();
                profile.getProperties().putAll(Minecraft.getMinecraft().func_181037_M());
                map.putAll(Minecraft.getMinecraft().getSessionService().getTextures(profile, false));
            }

            try {
                if (future != null) {
                    future.thenAccept(success -> {
                        if (!success && map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
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
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            if (future != null && !future.isDone()) {
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (InterruptedException | TimeoutException | ExecutionException exception) {
                    future.complete(false);
                }
            }
        });
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
            }, future);
            Minecraft.getMinecraft().addScheduledTask(() -> HDSkinManager.this.textureManager.loadTexture(resourcelocation, texture));
        }
    }
}