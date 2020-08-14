package de.hdskins.labymod.v112.resources;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import java.util.HashMap;
import java.util.Map;

public class InternalMinecraftProfileTexture extends MinecraftProfileTexture {

    public InternalMinecraftProfileTexture(String url, String hash) {
        super(url, null);
        this.hash = hash + System.currentTimeMillis();
    }

    private final String hash;
    private Map<String, String> metadata;

    @Override
    public String getHash() {
        return this.hash;
    }

    @Override
    public String getMetadata(String key) {
        return metadata == null ? null : metadata.get(key);
    }

    public Map<String, String> getMetadata() {
        return metadata == null ? (metadata = new HashMap<>()) : metadata;
    }
}
