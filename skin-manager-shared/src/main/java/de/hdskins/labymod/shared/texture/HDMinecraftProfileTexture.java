/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 - 2021 HD-Skins <https://github.com/HDSkins>
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

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import de.hdskins.labymod.shared.manager.SkinHashWrapper;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkinId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class HDMinecraftProfileTexture extends MinecraftProfileTexture {

  private static final Map<String, String> SLIM = ImmutableMap.of("model", "slim");

  protected HDMinecraftProfileTexture(String hash, @Nullable Map<String, String> metadata) {
    super(hash, metadata);
  }

  @Nonnull
  public static HDMinecraftProfileTexture texture(@Nonnull SkinHashWrapper wrapper) {
    return texture(wrapper.getSkinHash(), wrapper.isSlim() ? SLIM : null);
  }

  @Nonnull
  public static HDMinecraftProfileTexture texture(@Nonnull PacketServerResponseSkinId packet) {
    return texture(packet.getSkinId(), packet.isSlim() ? SLIM : null);
  }

  @Nonnull
  public static HDMinecraftProfileTexture texture(@Nonnull String hash) {
    return texture(hash, null);
  }

  @Nonnull
  public static HDMinecraftProfileTexture texture(@Nonnull String hash, @Nullable Map<String, String> metadata) {
    return new HDMinecraftProfileTexture(hash, metadata);
  }

  @Override
  public String getHash() {
    // Easy workaround
    return super.getUrl();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof HDMinecraftProfileTexture)) {
      return false;
    }

    return ((HDMinecraftProfileTexture) obj).getHash().equals(this.getHash());
  }

  @Override
  public int hashCode() {
    return this.getHash().hashCode();
  }
}
