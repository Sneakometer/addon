package de.hdskins.labymod.v18.manager;

import de.hdskins.labymod.shared.ReflectionUtils;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.mappings.Mappings;
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

        ReflectionUtils.set(
                Minecraft.class,
                Minecraft.getMinecraft(),
                new HDSkinManager(Minecraft.getMinecraft().getTextureManager(), cacheDir, Minecraft.getMinecraft().getSessionService(), configObject),
                mappings.getSkinManagerMappings()
        );
    }
}
