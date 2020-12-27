/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
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
package de.hdskins.labymod.shared.text;

import net.labymod.main.LabyMod;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault final class DefaultTextLine implements TextLine {

  private final FontRenderer fontRenderer;
  private String plain;
  private boolean centered;
  private int width;

  DefaultTextLine(String plain, boolean centered, FontRenderer fontRenderer) {
    this.plain = plain;
    this.centered = centered;
    this.width = fontRenderer.getStringWidth(plain);
    this.fontRenderer = fontRenderer;
  }

  @Nonnull
  @Override
  public String getPlainText() {
    return this.plain;
  }

  @Override
  public @Nonnull
  TextLine setPlainText(String plainText) {
    this.plain = plainText;
    this.width = this.fontRenderer.getStringWidth(plainText);
    return this;
  }

  @Override
  public boolean isCentered() {
    return this.centered;
  }

  @Override
  public @Nonnull
  TextLine setCentered(boolean centered) {
    this.centered = centered;
    return this;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public void draw(int entryX, int spaceRight, int y, int color) {
    if (this.centered) {
      int fullWidth = LabyMod.getInstance().getDrawUtils().getWidth() - spaceRight;
      entryX += (fullWidth / 2) - (this.width / 2);
    }
    this.fontRenderer.drawStringWithShadow(this.plain, entryX, y, color);
  }
}
