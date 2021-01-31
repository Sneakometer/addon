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
package de.hdskins.labymod.shared.settings.cache;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import de.hdskins.labymod.shared.utils.LabyModUtils;
import net.labymod.utils.Consumer;

public class CacheInvalidateClickHandler implements Constants, Consumer<ButtonElement> {

  private final AddonContext addonContext;

  public CacheInvalidateClickHandler(AddonContext addonContext) {
    this.addonContext = addonContext;
  }

  @Override
  public void accept(ButtonElement buttonElement) {
    this.addonContext.getSkinManager().invalidateAllSkins();
    LabyModUtils.displayAchievement(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("invalidate-skin-success"));
  }
}
