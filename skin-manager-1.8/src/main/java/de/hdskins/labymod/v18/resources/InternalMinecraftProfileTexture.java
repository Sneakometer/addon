package de.hdskins.labymod.v18.resources;

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

    public Map<String, String> getMetadata() {
        return metadata == null ? (metadata = new HashMap<>()) : metadata;
    }

    @Override
    public String getMetadata(String key) {
        if (metadata == null) {
            return null;
        }

        return metadata.get(key);
    }
}
