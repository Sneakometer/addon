package de.hdskins.labymod.core.gui;

import net.labymod.settings.elements.SettingsElement;

public class HdSkinManageElement extends SettingsElement {

    public HdSkinManageElement() {
        super(null, null);
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        this.mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;
    }

    @Override
    public void drawDescription(int x, int y, int screenWidth) {
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void unfocus(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public int getEntryHeight() {
        return 100;
    }
}
