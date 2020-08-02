package de.hdskins.labymod.resources;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

public class InternalMinecraftProfileTexture extends MinecraftProfileTexture {

    private final String hash;

    public InternalMinecraftProfileTexture(String url, String hash) {
        super(url, null);
        this.hash = hash;
    }

    @Override
    public String getHash() {
        return this.hash;
    }
}
