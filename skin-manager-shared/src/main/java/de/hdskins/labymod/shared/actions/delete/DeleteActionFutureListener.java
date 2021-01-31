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
package de.hdskins.labymod.shared.actions.delete;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.utils.LabyModUtils;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketServerQueryResponse;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class DeleteActionFutureListener implements FutureListener<PacketBase>, Constants {

  private final AddonContext addonContext;
  private final EntityPlayer targetDelete;

  protected DeleteActionFutureListener(AddonContext addonContext, EntityPlayer targetDelete) {
    this.addonContext = addonContext;
    this.targetDelete = targetDelete;
  }

  @Override
  public void nullResult() {
    LabyModUtils.displayAchievement(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-error"));
  }

  @Override
  public void nonNullResult(PacketBase packetBase) {
    if (packetBase instanceof PacketServerQueryResponse) {
      PacketServerQueryResponse response = (PacketServerQueryResponse) packetBase;
      if (response.isSuccess()) {
        LabyModUtils.displayAchievement(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-successfully", this.targetDelete.getName()));
      } else {
        LabyModUtils.displayAchievement(FAILURE, this.addonContext.getTranslationRegistry().translateMessage(response.getReason(), response.getReason()));
      }
    } else {
      LabyModUtils.displayAchievement(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-error"));
    }
  }

  @Override
  public void cancelled() {
    LabyModUtils.displayAchievement(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-error"));
  }
}
