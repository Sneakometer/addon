package de.hdskins.labymod.v18.manager;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.UUID;

public class SkinMap extends HashMap<UUID, ResourceLocation> {

    private final HDSkinManager skinManager;

    public SkinMap(HDSkinManager skinManager) {
        this.skinManager = skinManager;
    }

    @Override
    public ResourceLocation get(Object key) {
        return this.skinManager.loadSkinLocation((UUID) key);
    }

    @Override
    public ResourceLocation put(UUID key, ResourceLocation value) {
        return null;
    }
}
