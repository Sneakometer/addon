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
package de.hdskins.labymod.shared.asm.draw;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

// This class is a wrapper for the actual skull renderer, introduced for forge 1.12 support.
// The addon is build using the notch mappings, forge has a runtime re-obf in
// net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
// which transfers the code to searge, causes NoClassDefFoundErrors when a minecraft
// class is used in a not re-obf class. To prevent this, all methods in this class are
// called using no minecraft-internal classes but calls the methods with minecraft-internal
// usages from here.
public final class SkullRenderer {

  private SkullRenderer() {
    throw new UnsupportedOperationException();
  }

  public static void renderSkull(GameProfile gameProfile) {
    InternalSkullRenderer.renderSkull(gameProfile);
  }

  public static void drawPlayerHead(GameProfile gameProfile, int x, int y, int size) {
    InternalSkullRenderer.drawPlayerHead(gameProfile, x, y, size);
  }

  public static void drawPlayerHead(UUID uniqueId, int x, int y, int size) {
    InternalSkullRenderer.drawPlayerHead(uniqueId, x, y, size);
  }

  public static void drawPlayerHead(String username, int x, int y, int size) {
    InternalSkullRenderer.drawPlayerHead(username, x, y, size);
  }
}
