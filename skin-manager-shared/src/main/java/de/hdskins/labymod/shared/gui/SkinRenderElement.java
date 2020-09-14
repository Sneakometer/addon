package de.hdskins.labymod.shared.gui;

import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.Side;
import net.labymod.settings.elements.SettingsElement;

public class SkinRenderElement extends SettingsElement {

    private final MinecraftAdapter minecraftAdapter;
    private final Side side;

    public SkinRenderElement(MinecraftAdapter minecraftAdapter, Side side) {
        super("", "");
        this.minecraftAdapter = minecraftAdapter;
        this.side = side;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        this.renderPlayer(maxX);
    }

    private void renderPlayer(int maxX) {
        double rot = ((double) System.currentTimeMillis() / 25) % 360;

        int x = (maxX + maxX / 5) * this.side.getModifier();
        int y = ((this.minecraftAdapter.getWindowHeight() / 4) * 3) + 40;

        this.minecraftAdapter.renderPlayer(x, y, 0, 0, this.minecraftAdapter.getWindowHeight() / 5, (int) rot);
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
