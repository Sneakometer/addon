/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.hdskins.labymod.shared.texture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class HDSkinManager extends SkinManager {

    private final Path skinCacheDirectory;

    public HDSkinManager(File skinCacheDirectory) {
        super(Minecraft.getMinecraft().getTextureManager(), skinCacheDirectory, Minecraft.getMinecraft().getSessionService());
        this.skinCacheDirectory = skinCacheDirectory.toPath();
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type) {
        return this.loadSkin(texture, type, null);
    }

    @Override
    public ResourceLocation loadSkin(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type, SkinAvailableCallback callback) {
        return null;
    }

    @Override
    public void loadProfileTextures(GameProfile profile, SkinAvailableCallback callback, boolean requireSecure) {
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        return null;
    }
}
