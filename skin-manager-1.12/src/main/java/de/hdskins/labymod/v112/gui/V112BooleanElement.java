package de.hdskins.labymod.v112.gui;

import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class V112BooleanElement extends BooleanElement {

    private final Function<Boolean, CompletableFuture<Boolean>> toggleListener;
    private final AtomicBoolean currentValue;

    private String stringEnabled;
    private String stringDisabled;
    private GuiButton buttonToggle;

    public V112BooleanElement(String displayName, IconData iconData, String on, String off, boolean currentValue, Function<Boolean, CompletableFuture<Boolean>> toggleListener) {
        super(displayName, iconData, null, currentValue);
        this.stringEnabled = on;
        this.stringDisabled = off;
        this.currentValue = new AtomicBoolean(currentValue);
        this.toggleListener = toggleListener;
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
        if (this.buttonToggle.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            if (this.toggleListener != null) {
                this.toggleListener.apply(!this.currentValue.get()).thenAccept(this::setCurrentValue);
            } else {
                this.currentValue.set(!this.currentValue.get());
            }

            this.buttonToggle.playPressSound(this.mc.getSoundHandler());
        }
    }

    @Override
    public boolean getCurrentValue() {
        return currentValue.get();
    }

    public void setCurrentValue(boolean currentValue) {
        this.currentValue.set(currentValue);
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
