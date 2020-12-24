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
package de.hdskins.labymod.v1855;

import com.google.common.base.Preconditions;
import de.hdskins.labymod.shared.gui.AcceptRejectGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
public class AcceptRejectGuiScreenImpl extends AcceptRejectGuiScreen {

    static void init() {
        factory = (acceptText, rejectText, messageLines, callback) -> new AcceptRejectGuiScreenImpl(acceptText, rejectText, messageLines, callback);
    }

    protected AcceptRejectGuiScreenImpl(String acceptText, String rejectText, Collection<String> messageLines, BiConsumer<AcceptRejectGuiScreen, Boolean> callback) {
        super(acceptText, rejectText, messageLines, callback);
    }

    @Override
    public synchronized void requestFocus() {
        final Minecraft minecraft = this.mc == null ? Minecraft.getMinecraft() : this.mc;
        if (minecraft.currentScreen != this) {
            this.before = minecraft.currentScreen;
            minecraft.displayGuiScreen(this);
        }
    }

    @Override
    public synchronized void returnBack(@Nullable GuiScreen target) {
        // if this.mc == null this screen was never shown before
        if (this.mc != null && this.mc.currentScreen == this) {
            this.mc.displayGuiScreen(target == null ? this.before : target);
            this.before = null;
        }
    }

    @Nonnull
    @Override
    protected FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public void drawString(String line, int x, int y, int color) {
        this.fontRendererObj.drawStringWithShadow(line, x, y, color);
    }

    @Nonnull
    @Override
    public Collection<String> listFormattedStringToWidth(String line, int width) {
        return line.trim().isEmpty() ? SINGLE_STRING_LIST : this.fontRendererObj.listFormattedStringToWidth(line, width);
    }

    @Override
    protected void checkInitialized() {
        Preconditions.checkNotNull(this.fontRendererObj, "Illegal call to drawText() in %s", this.getClass().getName());
    }

    @Override
    protected void drawButtons(int mouseX, int mouseY, float partialTicks) {
        this.acceptButton.drawButton(this.mc, mouseX, mouseY);
        this.rejectButton.drawButton(this.mc, mouseX, mouseY);
    }
}
