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
package de.hdskins.labymod.shared.manager;

import de.hdskins.labymod.shared.texture.HDMinecraftProfileTexture;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkinId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SkinHashWrapper {

    private String skinHash;
    private boolean slim;

    @Nonnull
    public static SkinHashWrapper newEmpty() {
        return new SkinHashWrapper();
    }

    @Nonnull
    public static SkinHashWrapper wrap(@Nonnull PacketServerResponseSkinId packet) {
        return wrap(packet.getSkinId(), packet.isSlim());
    }

    @Nonnull
    public static SkinHashWrapper wrap(@Nonnull String skinHash) {
        return wrap(skinHash, Boolean.FALSE);
    }

    @Nonnull
    public static SkinHashWrapper wrap(@Nonnull String skinHash, boolean slim) {
        return new SkinHashWrapper().setSkinHash(skinHash).setSlim(slim);
    }

    public boolean hasSkin() {
        return this.skinHash != null;
    }

    public String getSkinHash() {
        return this.skinHash;
    }

    @Nonnull
    public SkinHashWrapper setSkinHash(@Nullable String skinHash) {
        this.skinHash = skinHash;
        return this;
    }

    public boolean isSlim() {
        return this.slim;
    }

    @Nonnull
    public SkinHashWrapper setSlim(boolean slim) {
        this.slim = slim;
        return this;
    }

    @Nonnull
    public HDMinecraftProfileTexture toProfileTexture() {
        return HDMinecraftProfileTexture.texture(this);
    }
}
