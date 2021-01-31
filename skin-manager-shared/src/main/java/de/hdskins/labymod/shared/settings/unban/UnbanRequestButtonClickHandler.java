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
package de.hdskins.labymod.shared.settings.unban;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.settings.countdown.ButtonCountdownElementNameChanger;
import de.hdskins.labymod.shared.settings.countdown.SettingsCountdownRegistry;
import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import de.hdskins.labymod.shared.utils.LabyModUtils;
import de.hdskins.labymod.shared.utils.UnbanRequestUtils;
import net.labymod.utils.Consumer;

import java.util.concurrent.atomic.AtomicBoolean;

public class UnbanRequestButtonClickHandler implements Consumer<ButtonElement>, Constants {

  private final AddonContext addonContext;
  private final AtomicBoolean actionRunning = new AtomicBoolean();

  public UnbanRequestButtonClickHandler(AddonContext addonContext) {
    this.addonContext = addonContext;
  }

  @Override
  public void accept(ButtonElement buttonElement) {
    if (!this.actionRunning.getAndSet(true)) {
      UnbanRequestUtils.tryOpenRequest(this.addonContext).thenAccept(result -> {
        LabyModUtils.displayAchievement(result.isSuccess() ? SUCCESS : FAILURE, result.getMessage());
        this.actionRunning.set(false);
      });
      if (!this.addonContext.getRole().isHigherOrEqualThan(UserRole.STAFF)) {
        SettingsCountdownRegistry.registerTask(
          new ButtonCountdownElementNameChanger(buttonElement),
          60
        );
      }
    }
  }
}
