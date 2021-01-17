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
package de.hdskins.labymod.shared.asm.debug;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;

import javax.annotation.Nonnull;
import java.util.List;

public final class DebugScreenUtils {

  private static final String FORMAT_LINE_1 = "HDSkins v%d, active: %b, reconnecting: %b";
  private static final String FORMAT_LINE_2 = "%d mojang, %d texture locations";
  private static final String FORMAT_LINE_3 = "%d uuid to skin-id, %d waiting unloads";

  private DebugScreenUtils() {
    throw new UnsupportedOperationException();
  }

  public static void appendScreenInfo(@Nonnull List<String> list) {
    final SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
    if (skinManager instanceof HDSkinManager) {
      appendScreenInfo(list, (HDSkinManager) skinManager);
    }
  }

  private static void appendScreenInfo(@Nonnull List<String> list, @Nonnull HDSkinManager manager) {
    final AddonContext addonContext = manager.getAddonContext();

    list.add(" ");
    list.add(String.format(FORMAT_LINE_1, Constants.getAddonInfo().getVersion(), addonContext.getActive().get(), addonContext.getReconnecting().get()));
    list.add(String.format(FORMAT_LINE_2, manager.mojangSkinCacheSize(), manager.textureToLocationCacheSize()));
    list.add(String.format(FORMAT_LINE_3, manager.uuidToWrapperCache(), manager.queuedUnloads()));
  }
}
