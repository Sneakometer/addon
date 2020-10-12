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
package de.hdskins.labymod.shared.actions;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketServerReportSkinResponse;
import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ReportUserActionEntry extends UserActionEntry implements ActionConstants {

    private final AddonContext addonContext;

    public ReportUserActionEntry(AddonContext addonContext) {
        super(
            addonContext.getTranslationRegistry().translateMessage("user-skin-report-choose-display-name"),
            EnumActionType.NONE,
            null,
            null
        );
        this.addonContext = addonContext;
    }

    @Override
    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
        this.addonContext.reportSkin(entityPlayer.getGameProfile().getId()).getFuture().addListener(new FutureListener<PacketBase>() {
            @Override
            public void nullResult() {
                ReportUserActionEntry.this.showMessage("user-skin-report-failed-unknown");
            }

            @Override
            public void nonNullResult(PacketBase packetBase) {
                if (packetBase instanceof PacketServerReportSkinResponse) {
                    PacketServerReportSkinResponse result = (PacketServerReportSkinResponse) packetBase;
                    if (result.isSuccess()) {
                        ReportUserActionEntry.this.showMessage("user-skin-report-success", entityPlayer.getName());
                    } else {
                        ReportUserActionEntry.this.showMessage("user-skin-report-success", entityPlayer.getName());
                    }
                }
            }

            @Override
            public void cancelled() {
                ReportUserActionEntry.this.showMessage("user-skin-report-failed-unknown");
            }
        });
    }

    private void showMessage(String key, Object... replacements) {
        LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage(key, replacements));
    }
}
