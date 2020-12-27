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
package de.hdskins.labymod.shared.settings.toggle;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.notify.NotificationUtil;
import de.hdskins.labymod.shared.settings.element.elements.ChangeableBooleanElement;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@ParametersAreNonnullByDefault
public class SkinToggleButtonClickHandler implements BiFunction<ChangeableBooleanElement, Boolean, CompletableFuture<Boolean>>, Constants {

  private final AddonContext addonContext;

  public SkinToggleButtonClickHandler(AddonContext addonContext) {
    this.addonContext = addonContext;
  }

  @Override
  public CompletableFuture<Boolean> apply(ChangeableBooleanElement element, Boolean showSkins) {
    this.addonContext.getAddonConfig().setShowSkinsOfOtherPlayers(showSkins);
    if (showSkins) {
      this.addonContext.getSkinManager().invalidateAllSkins();
      NotificationUtil.notify(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("show-all-skins-enabled"));
    } else {
      this.addonContext.getSkinManager().invalidateAll();
      NotificationUtil.notify(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("show-all-skins-disabled"));
    }

    return CompletableFuture.completedFuture(showSkins);
  }
}
