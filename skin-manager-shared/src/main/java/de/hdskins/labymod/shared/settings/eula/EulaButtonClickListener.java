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
package de.hdskins.labymod.shared.settings.eula;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.gui.AcceptRejectGuiScreen;
import de.hdskins.labymod.shared.utils.GuidelineUtils;

public class EulaButtonClickListener implements Runnable {

  private final AddonContext addonContext;

  public EulaButtonClickListener(AddonContext addonContext) {
    this.addonContext = addonContext;
  }

  @Override
  public void run() {
    AcceptRejectGuiScreen.newScreen(
      "Accept", "Decline",
      GuidelineUtils.readGuidelines(this.addonContext.getAddonConfig().getGuidelinesUrl()),
      (screen, accepted) -> {
        this.addonContext.getAddonConfig().setGuidelinesAccepted(accepted);
        screen.returnBack();
      }
    ).requestFocus();
  }
}
