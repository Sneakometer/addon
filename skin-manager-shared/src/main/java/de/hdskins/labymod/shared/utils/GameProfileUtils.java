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
package de.hdskins.labymod.shared.utils;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class GameProfileUtils {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
        .create();

    private GameProfileUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static UUID getUniqueId(@Nonnull GameProfile profile) {
        final Property textureProperty = Iterables.getFirst(profile.getProperties().get("textures"), null);
        if (textureProperty == null) {
            return profile.getId();
        }

        try {
            final String json = new String(Base64.getDecoder().decode(textureProperty.getValue()), StandardCharsets.UTF_8);
            final MinecraftTexturesPayload result = GSON.fromJson(json, MinecraftTexturesPayload.class);
            return result == null || result.getProfileId() == null ? profile.getId() : result.getProfileId();
        } catch (JsonParseException exception) {
            return profile.getId();
        }
    }
}
