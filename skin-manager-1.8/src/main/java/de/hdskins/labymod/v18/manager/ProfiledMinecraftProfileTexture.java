package de.hdskins.labymod.v18.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import java.util.Map;

public class ProfiledMinecraftProfileTexture extends MinecraftProfileTexture {

    private final GameProfile profile;
    private final Map<String, String> metadata;

    public ProfiledMinecraftProfileTexture(GameProfile profile, String url, Map<String, String> metadata) {
        super(url, metadata);
        this.profile = profile;
        this.metadata = metadata;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }
}
