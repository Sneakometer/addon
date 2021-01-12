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

import de.hdskins.labymod.shared.settings.element.PermanentElement;
import de.hdskins.labymod.shared.utils.ClientUtils;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class EulaButtonElement extends ControlElement implements PermanentElement {

  private static final ResourceLocation PAPER_LOCATION = new ResourceLocation("textures/items/paper.png");
  private final Runnable clickListener;
  private boolean permanent;

  public EulaButtonElement(Runnable clickListener) {
    super(null, null);
    this.clickListener = clickListener;
  }

  @Override
  public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
    final int height = LabyMod.getInstance().getDrawUtils().getHeight() - 20;
    final int width = LabyMod.getInstance().getDrawUtils().getWidth();

    this.mouseOver = mouseX <= width - 5 && mouseX >= width - 30 && mouseY <= height - 17 && mouseY >= height - 40;
    final int add = this.isMouseOver() ? 1 : 0;

    ClientUtils.resetColor();
    Minecraft.getMinecraft().getTextureManager().bindTexture(PAPER_LOCATION);
    LabyMod.getInstance().getDrawUtils().drawTexture(
      width - 30 - add,
      height - 40 - add,
      255,
      255,
      25 + add * 2,
      25 + add * 2
    );
    if (this.isMouseOver()) {
      LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, "EULA");
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (this.isMouseOver()) {
      this.clickListener.run();
    }
  }

  @Override
  public boolean isPermanent() {
    return this.permanent;
  }

  @Override
  public void setPermanent(boolean permanent) {
    this.permanent = permanent;
  }
}
