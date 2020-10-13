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
package de.hdskins.labymod.shared.actions.delete;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketServerReportSkinResponse;
import net.labymod.main.LabyMod;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class DeleteActionFutureListener implements FutureListener<PacketBase> {

    private final AddonContext addonContext;
    private final EntityPlayer targetDelete;

    protected DeleteActionFutureListener(AddonContext addonContext, EntityPlayer targetDelete) {
        this.addonContext = addonContext;
        this.targetDelete = targetDelete;
    }

    @Override
    public void nullResult() {
        LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-error"));
    }

    @Override
    public void nonNullResult(PacketBase packetBase) {
        if (packetBase instanceof PacketServerReportSkinResponse) {
            PacketServerReportSkinResponse response = (PacketServerReportSkinResponse) packetBase;
            if (response.isSuccess()) {
                LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-successfully", this.targetDelete.getName()));
            } else {
                LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage(response.getReason(), response.getReason()));
            }
        } else {
            LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-error"));
        }
    }

    @Override
    public void cancelled() {
        LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("team-delete-skin-error"));
    }
}
