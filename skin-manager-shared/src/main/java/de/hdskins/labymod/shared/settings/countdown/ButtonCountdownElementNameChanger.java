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
package de.hdskins.labymod.shared.settings.countdown;

import com.google.common.primitives.Longs;
import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import net.labymod.core.LabyModCore;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@SuppressWarnings("UnstableApiUsage")
public class ButtonCountdownElementNameChanger extends DefaultCountdownElementNameChanger {

  private final ButtonElement buttonElement;
  private String previousName;
  private int stringWidth;

  public ButtonCountdownElementNameChanger(ButtonElement targetElement) {
    super(targetElement);
    this.buttonElement = targetElement;
    this.setPreviousName(this.buttonElement.getText());
  }

  @Override
  public void accept(Long remainingTime) {
    if (Longs.tryParse(this.buttonElement.getText().replace("§c§l", "")) == null) {
      this.setPreviousName(this.buttonElement.getText());
    }

    if (remainingTime <= 0) {
      this.buttonElement.setSettingEnabled(true);
      this.buttonElement.setText(this.previousName);
    } else {
      this.buttonElement.setText("§c§l" + remainingTime, this.stringWidth);
    }
  }

  private void setPreviousName(String previousName) {
    this.previousName = previousName;
    this.stringWidth = LabyModCore.getMinecraft().getFontRenderer().getStringWidth(previousName);
  }
}
