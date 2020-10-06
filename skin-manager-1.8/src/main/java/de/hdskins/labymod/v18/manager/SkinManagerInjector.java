package de.hdskins.labymod.v18.manager;

import de.hdskins.labymod.test.ReflectionUtils;
import de.hdskins.labymod.test.config.ConfigObject;
import de.hdskins.labymod.test.mappings.Mappings;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;

import java.io.File;

public final class SkinManagerInjector {

    private SkinManagerInjector() {
        throw new UnsupportedOperationException();
    }

    public static void setNewSkinManager(ConfigObject configObject, Mappings mappings) {
        File cacheDir = ReflectionUtils.get(File.class, SkinManager.class, Minecraft.getMinecraft().getSkinManager(), mappings.getSkinCacheDirMappings());
        if (cacheDir == null) {
            System.err.println("Unable to find cache dir variable");
            return;
        }

        HDSkinManager skinManager = new HDSkinManager(configObject, Minecraft.getMinecraft().getTextureManager(), cacheDir, Minecraft.getMinecraft().getSessionService());
        ReflectionUtils.set(Minecraft.class, Minecraft.getMinecraft(), skinManager, mappings.getSkinManagerMappings());

        try {
            DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

            Class<?> textureCacheClass = Class.forName("net.labymod.utils.texture.PlayerSkinTextureCache");
            Object textureCache = ReflectionUtils.get(null, DrawUtils.class, drawUtils, "playerSkinTextureCache");

            //ReflectionUtils.set(textureCacheClass, textureCache, new SkinMap(skinManager), "loadedSkins"); TODO
            ReflectionUtils.set(textureCacheClass, textureCache, skinManager, "skinManager");
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }
}
