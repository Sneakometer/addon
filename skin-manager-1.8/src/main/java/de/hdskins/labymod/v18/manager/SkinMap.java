package de.hdskins.labymod.v18.manager;

import net.minecraft.util.ResourceLocation;

import java.util.*;

public class SkinMap implements Map<UUID, ResourceLocation> {

    private final HDSkinManager skinManager;

    public SkinMap(HDSkinManager skinManager) {
        this.skinManager = skinManager;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return true;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public ResourceLocation get(Object key) {
        return this.skinManager.getSkinLocation((UUID) key);
    }

    @Override
    public ResourceLocation put(UUID key, ResourceLocation value) {
        return null;
    }

    @Override
    public ResourceLocation remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends UUID, ? extends ResourceLocation> m) {
    }

    @Override
    public void clear() {
    }

    @Override
    public Set<UUID> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ResourceLocation> values() {
        return Collections.emptyList();
    }

    @Override
    public Set<Entry<UUID, ResourceLocation>> entrySet() {
        return Collections.emptySet();
    }
}
