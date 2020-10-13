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
package de.hdskins.labymod.shared.settings.element.elements;

import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class ButtonElement extends ControlElement {

    private final GuiButton button = new GuiButton(-2, 0, 0, 0, 20, "");
    private final Runnable clickListener;
    private boolean enabled;

    public ButtonElement(String displayName, ControlElement.IconData iconData, String inButtonName, Runnable clickListener) {
        super(displayName, iconData);
        this.button.displayString = inButtonName;
        this.clickListener = clickListener;
    }

    public String getText() {
        return this.button.displayString;
    }

    public void setText(String text) {
        this.button.displayString = text;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.button.mousePressed(this.mc, mouseX, mouseY)) {
            this.button.playPressSound(super.mc.getSoundHandler());
            this.clickListener.run();
        }
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        if (super.displayName != null) {
            LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, Color.GRAY.getRGB());
        }

        int buttonWidth = super.displayName == null ? maxX - x : LabyModCore.getMinecraft().getFontRenderer().getStringWidth(this.button.displayString) + 20;

        this.button.setWidth(buttonWidth);
        this.button.enabled = this.enabled;

        LabyModCore.getMinecraft().setButtonXPosition(this.button, maxX - buttonWidth - 2);
        LabyModCore.getMinecraft().setButtonYPosition(this.button, y + 1);

        LabyModCore.getMinecraft().drawButton(this.button, mouseX, mouseY);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.button.enabled = enabled;
    }
}
