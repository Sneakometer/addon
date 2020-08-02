package de.hdskins.labymod.manager;

import de.hdskins.labymod.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;

import java.io.File;

public final class SkinManagerInjector {

    private SkinManagerInjector() {
        throw new UnsupportedOperationException();
    }

    public static void setNewSkinManager() {
        File cacheDir = ReflectionUtils.get(File.class, SkinManager.class, Minecraft.getMinecraft().getSkinManager(), "c", "skinCacheDir");
        if (cacheDir == null) {
            System.err.println("Unable to find cache dir variable");
            return;
        }

        ReflectionUtils.set(
                Minecraft.class,
                Minecraft.getMinecraft(),
                new HDSkinManager(Minecraft.getMinecraft().getTextureManager(), cacheDir, Minecraft.getMinecraft().getSessionService()),
                "aL", "skinManager"
        );
    }
}
