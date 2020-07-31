package de.hdskins.labymod.asm;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import java.util.Map;

public class HdTextureProvider {

    public static void fillProperties(GameProfile profile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {

        textures.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture("", null));
    }

}
