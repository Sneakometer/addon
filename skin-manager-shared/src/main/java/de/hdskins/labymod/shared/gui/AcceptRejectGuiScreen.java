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

import de.hdskins.labymod.shared.Constants;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
public abstract class AcceptRejectGuiScreen extends GuiScreen {

    protected static final Collection<String> SINGLE_STRING_LIST = Collections.singleton(Constants.SPACE);
    protected static final int RIGHT_SPACE = 50;
    protected static AcceptRejectGuiScreenFactory factory;

    protected final String acceptText;
    protected final String rejectText;
    protected final Collection<String> rawMessageLines;
    protected final BiConsumer<AcceptRejectGuiScreen, Boolean> callback;
    // buttons (will be re-initialized when the gui get initialized, so every time f.ex. the window is resized)
    protected volatile GuiOptionButton acceptButton;
    protected volatile GuiOptionButton rejectButton;
    protected volatile List<TextLine> textLines;
    // Used by requestFocus() and returnBack()
    protected volatile GuiScreen before;
    protected final Scrollbar scrollbar = new Scrollbar(this.getFontRenderer().FONT_HEIGHT);

    public AcceptRejectGuiScreen(String acceptText, String rejectText, Collection<String> rawMessageLines, BiConsumer<AcceptRejectGuiScreen, Boolean> callback) {
        this.acceptText = acceptText;
        this.rejectText = rejectText;
        this.rawMessageLines = rawMessageLines;
        this.callback = callback;
    }

    @Nonnull
    public static AcceptRejectGuiScreen newScreen(String acceptText, String rejectText, Collection<String> messageLines, BiConsumer<AcceptRejectGuiScreen, Boolean> callback) {
        return factory.newScreen(acceptText, rejectText, messageLines, callback);
    }

    public abstract void requestFocus();

    public abstract void returnBack(@Nullable GuiScreen target);

    @Nonnull
    protected abstract FontRenderer getFontRenderer();

    public abstract void drawString(String line, int x, int y, int color);

    @Nonnull
    public abstract Collection<String> listFormattedStringToWidth(String line, int width);

    protected abstract void checkInitialized();

    protected abstract void drawButtons(int mouseX, int mouseY, float partialTicks);

    @Override
    public void initGui() {
        this.acceptButton = new GuiOptionButton(0, this.width / 2 - 155, this.height - 35, this.acceptText);
        this.rejectButton = new GuiOptionButton(1, this.width / 2 - 155 + 160, this.height - 35, this.rejectText);
        this.initializeText();
        // scrollbar
        this.scrollbar.setPosition(this.width - 16, 5, this.width - 10, this.height - 55);
        this.scrollbar.update(this.textLines.size());
        this.scrollbar.setSpeed(20);
        this.scrollbar.init();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
        this.scrollbar.draw();
        this.drawText();
        this.drawButtons(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
        if (clickedButton == 0) { // 0 is the left key of the mouse
            GuiButton clicked = null;
            if (this.acceptButton.mousePressed(this.mc, mouseX, mouseY)) {
                clicked = this.acceptButton;
            } else if (this.rejectButton.mousePressed(this.mc, mouseX, mouseY)) {
                clicked = this.rejectButton;
            }
            if (clicked != null) {
                this.actionPerformed(clicked);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int releasedButton) {
        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.callback.accept(this, this.acceptButton != null && button.id == this.acceptButton.id);
        this.returnBack(this.before);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.scrollbar.mouseInput();
    }

    protected void initializeText() {
        this.textLines = new ArrayList<>();
        for (String messageLine : this.rawMessageLines) {
            if (messageLine.trim().isEmpty()) {
                // hack:
                // To render a string minecraft internally visits each char of the string
                // if the string is empty minecraft just skips the rendering - we are preventing
                // this by providing a non-empty line
                this.textLines.add(TextLine.leftAligned(Constants.SPACE, this.getFontRenderer()));
            } else {
                TextLine line = TextLine.parse(messageLine, this.getFontRenderer());
                for (String formattedLine : this.listFormattedStringToWidth(line.getLine(), this.width - RIGHT_SPACE)) {
                    this.textLines.add(TextLine.of(formattedLine, this.getFontRenderer(), line.isCentered()));
                }
            }
        }
    }

    protected void drawText() {
        this.checkInitialized();
        // hack:
        // we disable bidi to prevent the render of color codes correctly
        final boolean bidi = this.getFontRenderer().getBidiFlag();
        this.getFontRenderer().setBidiFlag(false);
        // calculate the position flags for the actual render
        final int fontHeight = this.getFontRenderer().FONT_HEIGHT;
        final int maxLinesRenderAmount = Math.max(fontHeight, this.height - 55) / Math.max(1, fontHeight);
        final int textEntryPoint = (int) (Math.abs(this.scrollbar.getScrollY()) / Math.max(1, fontHeight));
        final int maxTextLinesListEntry = textEntryPoint + maxLinesRenderAmount;
        // the current height starts at 0 and is increased by the font height in the loop
        int currentHeightPosition = 0;
        for (int i = textEntryPoint; i < maxTextLinesListEntry; i++) {
            if (i >= this.textLines.size()) {
                break;
            }
            TextLine line = this.textLines.get(i);
            int x = 15;
            if (line.isCentered()) {
                int fullWidth = LabyMod.getInstance().getDrawUtils().getScaledResolution().getScaledWidth() - RIGHT_SPACE;
                x += (fullWidth / 2) - (line.getWidth() / 2);
            }

            this.drawString(line.getLine(), x, currentHeightPosition += fontHeight, 0xffffff);
        }
        // now we can reset the bidi flag of the font renderer
        this.getFontRenderer().setBidiFlag(bidi);
    }
}
