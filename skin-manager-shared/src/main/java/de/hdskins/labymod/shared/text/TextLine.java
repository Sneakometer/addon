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
package de.hdskins.labymod.shared.text;

import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface TextLine {

  String CENTERED_OPEN_TAG = "${CENTER}";

  @Nonnull
  static TextLine line(String text, FontRenderer renderer) {
    return new DefaultTextLine(text, Boolean.FALSE, renderer);
  }

  @Nonnull
  static TextLine line(String text, boolean centered, FontRenderer renderer) {
    return new DefaultTextLine(text, centered, renderer);
  }

  @Nonnull
  static TextLine centered(String text, FontRenderer renderer) {
    return new DefaultTextLine(text, Boolean.TRUE, renderer);
  }

  @Nonnull
  static TextLine parse(String text, FontRenderer renderer) {
    if (text.startsWith(CENTERED_OPEN_TAG)) {
      return new DefaultTextLine(text.replace(CENTERED_OPEN_TAG, ""), true, renderer);
    }
    return new DefaultTextLine(text, false, renderer);
  }

  @Nonnull
  String getPlainText();

  @Nonnull
  TextLine setPlainText(String plainText);

  boolean isCentered();

  @Nonnull
  TextLine setCentered(boolean centered);

  int getWidth();

  void draw(int entryX, int spaceRight, int y, int color);
}
