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
package de.hdskins.labymod.shared.utils;

import net.labymod.addon.About;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.main.LabyMod;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class LabyModUtils {

  private LabyModUtils() {
    throw new UnsupportedOperationException();
  }

  public static void displayAchievement(String title, String description) {
    LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(title, description);
  }

  @Nonnull
  public static AddonInfo getAddonInfo(About about) {
    // Try to find the addons in the online addons (where it should be normally)
    final AddonInfoManager addonInfoManager = AddonInfoManager.getInstance();
    // By default the manager isn't initialized right now so hack us in
    addonInfoManager.init();
    AddonInfo addonInfo = addonInfoManager.getAddonInfoMap().get(about.uuid);
    // The addon info wasn't at the online addons so try to find it in the offline ones
    if (addonInfo == null) {
      addonInfo = AddonLoader.getOfflineAddons().stream()
        .filter(addon -> addon.getUuid().equals(about.uuid))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unable to find addon info of \"" + about.name + "\" (" + about.uuid + ")!"));
    }
    return addonInfo;
  }
}
