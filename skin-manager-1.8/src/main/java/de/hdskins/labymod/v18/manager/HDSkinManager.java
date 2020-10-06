package de.hdskins.labymod.v18.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import de.hdskins.labymod.test.ReflectionUtils;
import de.hdskins.labymod.test.config.ConfigObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HDSkinManager extends SkinManager {

    private final ConfigObject config;
    private final Field textureMetadataField;
    private final TextureManager textureManager;
    private final File cacheDir;
    private final MinecraftSessionService sessionService;
    private final LoadingCache<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cache;

    public HDSkinManager(ConfigObject config, TextureManager textureManager, File cacheDir, MinecraftSessionService sessionService) {
        super(textureManager, cacheDir, sessionService);
        this.config = config;
        this.textureManager = textureManager;
        this.cacheDir = cacheDir;
        this.sessionService = sessionService;
        this.textureMetadataField = ReflectionUtils.getFieldByName(MinecraftProfileTexture.class, "metadata");
        this.cache = this.createCache();

        Arrays.stream(SkinManager.class.getDeclaredFields())
                .filter(field -> field.getType().equals(LoadingCache.class))
                .findFirst()
                .ifPresent(field -> ReflectionUtils.set(this, this.cache, field));
    }

    private LoadingCache<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> createCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.SECONDS)
                .build(new CacheLoader<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {
                    @Override
                    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(GameProfile profile) {
                        return HDSkinManager.this.loadMojangTextures(profile);
                    }
                });
    }

    private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadMojangTextures(GameProfile profile) {
        try {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> in = Minecraft.getMinecraft().getSessionService().getTextures(profile, false);
            if (in.isEmpty()) {
                return in;
            }

            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> result = new HashMap<>();

            for (Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture> entry : in.entrySet()) {
                MinecraftProfileTexture texture = entry.getValue();
                Map<String, String> metadata = (Map<String, String>) textureMetadataField.get(texture);

                result.put(entry.getKey(), new ProfiledMinecraftProfileTexture(profile, texture.getUrl(), metadata));
            }

            return result;
        } catch (Throwable ignored) {
            return Maps.newHashMap();
        }
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type, SkinAvailableCallback callback) {
        if (type != MinecraftProfileTexture.Type.SKIN || !(texture instanceof ProfiledMinecraftProfileTexture)) {
            return super.loadSkin(texture, type, callback);
        }

        ResourceLocation cacheLocation = new ResourceLocation("skins/" + texture.getHash());
        ITextureObject cached = this.textureManager.getTexture(cacheLocation);
        if (cached != null) {
            if (callback != null) {
                callback.skinAvailable(type, cacheLocation, texture);
            }
            return cacheLocation;
        }

        ProfiledMinecraftProfileTexture profiledTexture = (ProfiledMinecraftProfileTexture) texture;

        File cacheDir = new File(this.cacheDir, texture.getHash().length() > 2 ? texture.getHash().substring(0, 2) : "xx");
        File cacheFile = new File(cacheDir, texture.getHash());

        final IImageBuffer buffer = new WrappedImageBuffer(new ImageBufferDownload(), () -> {
            if (callback != null) {
                callback.skinAvailable(type, cacheLocation, texture);
            }
        });

        HDSkinDownloadImageData downloader = new HDSkinDownloadImageData(cacheFile, texture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), this.config, buffer, profiledTexture);
        this.textureManager.loadTexture(cacheLocation, downloader);

        return cacheLocation;
    }

    public ResourceLocation getSkinLocation(UUID uniqueId) {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = this.cache.getIfPresent(new GameProfile(uniqueId, "Steve"));
        if (map == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        // TODO
        return null;
    }

}
