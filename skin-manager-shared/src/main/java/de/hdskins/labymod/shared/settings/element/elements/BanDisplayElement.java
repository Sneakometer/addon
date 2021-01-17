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

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.settings.element.PermanentElement;
import de.hdskins.labymod.shared.utils.TimeUtils;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateBan;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;

public class BanDisplayElement extends ControlElement implements PermanentElement {

  private final AddonContext addonContext;
  private boolean permanent;

  public BanDisplayElement(AddonContext addonContext) {
    super(null, null);
    this.addonContext = addonContext;
  }

  @Override
  public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
    final PacketServerLiveUpdateBan currentBan = this.addonContext.getCurrentBan();
    if (currentBan != null) {
      final String remainingTime = currentBan.isPermanentlyBanned() ? " permanent" : TimeUtils.formatRemainingTime(currentBan.getTimeout());
      if (remainingTime != null) {
        String reason = currentBan.getReason();
        if (currentBan.isTranslate()) {
          reason = this.addonContext.getTranslationRegistry().translateMessage(currentBan.getReason());
        }
        final DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
        final String[] text = this.addonContext.getTranslationRegistry().translateMessage("currently-banned", reason, remainingTime).split("\n");

        int times = 0;
        for (String s : text) {
          drawUtils.drawCenteredString(s, drawUtils.getWidth() / 2d, y + 5 + (times > 0 ? 1 : 0) + (times++ * drawUtils.getFontRenderer().FONT_HEIGHT));
        }
      }
    }
  }

  @Override
  public int getEntryHeight() {
    final DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
    final String[] text = this.addonContext.getTranslationRegistry().translateMessage("currently-banned", "", "").split("\n");
    return 5 + (text.length * drawUtils.getFontRenderer().FONT_HEIGHT) + (text.length - 1);
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
