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
package de.hdskins.labymod.shared.settings.element.elements;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.event.UserBanUpdateEvent;
import de.hdskins.labymod.shared.eventbus.EventListener;
import net.labymod.utils.Consumer;

import javax.annotation.Nonnull;

public class UnbanRequestButtonElement extends ButtonElement {

  private final AddonContext addonContext;

  public UnbanRequestButtonElement(String displayName, IconData iconData, String inButtonName, Consumer<ButtonElement> clickListener, AddonContext addonContext) {
    super(displayName, iconData, inButtonName, clickListener);
    this.addonContext = addonContext;
    Constants.EVENT_BUS.registerListener(this);
  }

  @Override
  public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
    if (this.addonContext.getCurrentBan() != null) {
      super.draw(x, y, maxX, maxY, mouseX, mouseY);
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (this.addonContext.getCurrentBan() != null) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  @Override
  public int getEntryHeight() {
    return this.addonContext.getCurrentBan() != null ? super.getEntryHeight() : 0;
  }

  @EventListener
  public void handleBanUpdate(@Nonnull UserBanUpdateEvent event) {
    this.setSettingEnabled(event.isBanned());
  }
}
