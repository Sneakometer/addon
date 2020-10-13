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
package de.hdskins.labymod.shared.actions.report;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketServerReportSkinResponse;
import net.labymod.main.LabyMod;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class ReportActionFutureListener implements FutureListener<PacketBase> {

    private final AddonContext addonContext;
    private final EntityPlayer reported;

    protected ReportActionFutureListener(AddonContext addonContext, EntityPlayer reported) {
        this.addonContext = addonContext;
        this.reported = reported;
    }

    @Override
    public void nullResult() {
        LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("user-skin-report-failed-unknown"));
    }

    @Override
    public void nonNullResult(PacketBase packetBase) {
        if (packetBase instanceof PacketServerReportSkinResponse) {
            PacketServerReportSkinResponse result = (PacketServerReportSkinResponse) packetBase;
            if (result.isSuccess()) {
                LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("user-skin-report-success", this.reported.getName()));
            } else {
                LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage(result.getReason(), result.getReason()));
            }
        } else {
            LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("user-skin-report-failed-unknown"));
        }
    }

    @Override
    public void cancelled() {
        LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage("user-skin-report-failed-unknown"));
    }
}
