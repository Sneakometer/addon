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
import net.labymod.settings.elements.BooleanElement;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class ChangeableBooleanElement extends BooleanElement {

    private final BiFunction<ChangeableBooleanElement, Boolean, CompletableFuture<Boolean>> toggleListener;
    private final AtomicBoolean currentValue;

    private String stringEnabled;
    private String stringDisabled;
    private GuiButton buttonToggle;

    private boolean enabled = true;
    private boolean pressable = true;

    public ChangeableBooleanElement(String displayName, IconData iconData, String on, String off,
                                    boolean currentValue, BiFunction<ChangeableBooleanElement, Boolean, CompletableFuture<Boolean>> toggleListener) {
        super(displayName, iconData, null, currentValue);
        this.toggleListener = toggleListener;
        this.currentValue = new AtomicBoolean(currentValue);
        this.stringEnabled = on;
        this.stringDisabled = off;
        this.createButton();
    }

    @Override
    public void createButton() {
        if (this.currentValue == null) {
            return;
        }

        this.buttonToggle = new GuiButton(-2, 0, 0, 0, 20, "");
        this.setSettingEnabled(this.currentValue.get());
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);
        int width = this.getObjectWidth();
        if (this.buttonToggle != null) {
            this.buttonToggle.enabled = false;
            LabyModCore.getMinecraft().setButtonXPosition(this.buttonToggle, maxX - width - 2);
            LabyModCore.getMinecraft().setButtonYPosition(this.buttonToggle, y + 1);
            this.buttonToggle.setWidth(width);
            LabyModCore.getMinecraft().drawButton(this.buttonToggle, mouseX, mouseY);
            this.buttonToggle.enabled = true;
            int buttonWidth = this.buttonToggle.getButtonWidth();
            int valueXPos = this.currentValue.get() ? (buttonWidth - 4) / 2 : (buttonWidth - 4) / 2 + 6;
            String displayString = (this.buttonToggle.isMouseOver() ? ModColor.YELLOW : (this.currentValue.get() ? ModColor.WHITE : ModColor.GRAY)) + (this.currentValue.get() ? this.stringEnabled : this.stringDisabled);
            LabyMod.getInstance().getDrawUtils().drawCenteredString(displayString, LabyModCore.getMinecraft().getXPosition(this.buttonToggle) + valueXPos, LabyModCore.getMinecraft().getYPosition(this.buttonToggle) + 6);
            LabyMod.getInstance().getDrawUtils().drawString(this.currentValue.get() ? ModColor.GREEN.toString() : ModColor.RED.toString(), 0.0D, 0.0D);
            this.mc.getTextureManager().bindTexture(buttonTextures);
            int pos = (this.currentValue.get() ? maxX - 8 : maxX - width) - 2;
            LabyMod.getInstance().getDrawUtils().drawTexturedModalRect(pos, y + 1, 0, 66, 4, 20);
            LabyMod.getInstance().getDrawUtils().drawTexturedModalRect(pos + 4, y + 1, 196, 66, 4, 20);
            LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, this.currentValue.get() ? ModColor.toRGB(20, 120, 20, 120) : ModColor.toRGB(120, 20, 20, 120));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.enabled || !this.pressable) {
            return;
        }

        if (this.buttonToggle.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            boolean playSound = true;

            if (this.toggleListener != null) {
                CompletableFuture<Boolean> future = this.toggleListener.apply(this, !this.currentValue.get());
                if (future != null) {
                    this.pressable = false;
                    future.thenAccept(value -> {
                        this.pressable = true;
                        this.setCurrentValue(value);
                    });
                }

                playSound = future != null;
            } else {
                this.currentValue.set(!this.currentValue.get());
            }

            if (playSound) {
                this.buttonToggle.playPressSound(this.mc.getSoundHandler());
            }
        }
    }

    @Override
    public boolean getCurrentValue() {
        return this.currentValue.get();
    }

    public void setCurrentValue(boolean currentValue) {
        this.currentValue.set(currentValue);
    }

    @Override
    public void setSettingEnabled(boolean settingEnabled) {
        this.enabled = settingEnabled;
    }

    @Override
    public BooleanElement custom(String... args) {
        if (args.length >= 1) {
            this.stringEnabled = args[0];
        }

        if (args.length >= 2) {
            this.stringDisabled = args[1];
        }

        return this;
    }
}
