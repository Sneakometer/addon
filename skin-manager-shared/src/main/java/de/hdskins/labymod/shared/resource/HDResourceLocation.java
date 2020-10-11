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
package de.hdskins.labymod.shared.resource;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.util.ResourceLocation;

public class HDResourceLocation extends ResourceLocation {

    public static HDResourceLocation forProfileTexture(MinecraftProfileTexture texture) {
        final String hash = texture.getHash();
        final String hashPrefix = hash.length() > 2 ? hash.substring(0, 2) : "xx";
        return new HDResourceLocation("skins/" + hashPrefix + "/" + hash);
    }

    private static final String[] EMPTY = new String[]{"a", "b"};
    private static final String DOMAIN = "minecraft";
    private String resourcePath;

    public HDResourceLocation(String path) {
        super(0, EMPTY);
    }

    @Override
    public String getResourcePath() {
        return this.resourcePath;
    }

    @Override
    public String getResourceDomain() {
        return DOMAIN;
    }

    @Override
    public String toString() {
        return DOMAIN + ':' + this.resourcePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof HDResourceLocation)) {
            return false;
        } else {
            return this.resourcePath.equals(((HDResourceLocation) o).resourcePath);
        }
    }

    @Override
    public int hashCode() {
        return 31 * this.resourcePath.hashCode();
    }
}
