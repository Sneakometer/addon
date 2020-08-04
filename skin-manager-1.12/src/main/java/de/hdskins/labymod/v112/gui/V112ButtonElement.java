package de.hdskins.labymod.v112.gui;

import de.hdskins.labymod.shared.gui.ButtonElement;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class V112ButtonElement extends ButtonElement {

    private final GuiButton button = new GuiButton(-2, 0, 0, 0, 20, "");

    public V112ButtonElement(String displayName, ControlElement.IconData iconData, String inButtonName) {
        super(displayName, iconData);
        this.button.displayString = inButtonName;
    }

    @Override
    public String getText() {
        return this.button.displayString;
    }

    @Override
    public void setText(String text) {
        this.button.displayString = text;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.runnable != null && this.button.mousePressed(this.mc, mouseX, mouseY)) {
            this.button.playPressSound(super.mc.getSoundHandler());
            this.runnable.run();
        }
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        if (super.displayName != null) {
            LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, Color.GRAY.getRGB());
        }

        int buttonWidth = super.displayName == null ? maxX - x : super.mc.fontRenderer.getStringWidth(this.button.displayString) + 20;

        this.button.setWidth(buttonWidth);
        this.button.enabled = this.enabled;

        LabyModCore.getMinecraft().setButtonXPosition(this.button, maxX - buttonWidth - 2);
        LabyModCore.getMinecraft().setButtonYPosition(this.button, y + 1);

        LabyModCore.getMinecraft().drawButton(this.button, mouseX, mouseY);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.button.enabled = enabled;
    }
}
