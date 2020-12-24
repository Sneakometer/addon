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
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PlayerSkinRenderElement extends SettingsElement {

    public PlayerSkinRenderElement() {
        super("", "");
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);
        // If the player is not connected to a server the player is null
        final EntityPlayerSP player = LabyModCore.getMinecraft().getPlayer();
        if (player != null) {
            // Calculate the current position of the player as it should rotate
            final int windowHeight = LabyMod.getInstance().getDrawUtils().getHeight();
            final double currentRotation = (System.currentTimeMillis() / 25D) % 360;
            final int locationX = maxX + maxX / 5;
            final int locationY = ((windowHeight / 4) * 3) + 40;
            // We have to reset the color before we can render the color because it leads to problems if we don't
            GlStateManager.color(1, 1, 1, 1);
            // now we can draw the actual player
            DrawUtils.drawEntityOnScreen(locationX, locationY, windowHeight / 5, 0, 0, (int) currentRotation, 0, 0, player);
        }
    }

    @Override
    public void drawDescription(int i, int i1, int i2) {
    }

    @Override
    public void mouseClicked(int i, int i1, int i2) {
    }

    @Override
    public void mouseRelease(int i, int i1, int i2) {
    }

    @Override
    public void mouseClickMove(int i, int i1, int i2) {
    }

    @Override
    public void keyTyped(char c, int i) {
    }

    @Override
    public void unfocus(int i, int i1, int i2) {
    }

    @Override
    public int getEntryHeight() {
        return 0;
    }
}
