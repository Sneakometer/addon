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
package de.hdskins.labymod.shared.actions.toggle;

import de.hdskins.labymod.shared.actions.ActionConstants;
import de.hdskins.labymod.shared.actions.MarkedUserActionEntry;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.utils.LabyModUtils;
import net.labymod.user.User;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ToggleSkinUserActionEntry extends MarkedUserActionEntry implements ActionConstants {

  private final AddonContext addonContext;

  public ToggleSkinUserActionEntry(AddonContext addonContext) {
    super(
      addonContext.getTranslationRegistry().translateMessage("toggle-skin-button-name"),
      EnumActionType.NONE,
      null,
      null
    );
    this.addonContext = addonContext;
  }

  @Override
  public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
    boolean blacklisted = this.addonContext.getAddonConfig().isSkinDisabled(entityPlayer.getGameProfile().getId());
    if (blacklisted) {
      this.addonContext.getAddonConfig().enableSkin(entityPlayer.getGameProfile().getId());
      this.addonContext.getSkinManager().updateSkin(entityPlayer.getGameProfile().getId(), null);
      LabyModUtils.displayAchievement(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("toggle-skin-shown"));
    } else {
      this.addonContext.getAddonConfig().disableSkin(entityPlayer.getGameProfile().getId());
      this.addonContext.getSkinManager().pushSkinDelete(entityPlayer.getGameProfile().getId());
      LabyModUtils.displayAchievement(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("toggle-skin-hidden"));
    }
  }
}
