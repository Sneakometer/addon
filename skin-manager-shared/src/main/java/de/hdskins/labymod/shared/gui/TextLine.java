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
package de.hdskins.labymod.shared.gui;

import net.minecraft.client.gui.FontRenderer;

public class TextLine {

  private final String line;
  private final int width;
  private final boolean centered;

  private TextLine(String line, FontRenderer renderer, boolean centered) {
    this.line = line;
    this.width = renderer.getStringWidth(this.line);
    this.centered = centered;
  }

  public String getLine() {
    return this.line;
  }

  public boolean isCentered() {
    return this.centered;
  }

  public int getWidth() {
    return this.width;
  }

  public static TextLine of(String line, FontRenderer renderer, boolean centered) {
    return new TextLine(line, renderer, centered);
  }

  public static TextLine centered(String line, FontRenderer renderer) {
    return new TextLine(line, renderer, true);
  }

  public static TextLine leftAligned(String line, FontRenderer renderer) {
    return new TextLine(line, renderer, false);
  }

  public static TextLine parse(String rawLine, FontRenderer renderer) {
    boolean centered = false;

    if (rawLine.contains("${CENTER}")) {
      centered = true;
      rawLine = rawLine.replace("${CENTER}", "");
    }

    return new TextLine(rawLine, renderer, centered);
  }

}
