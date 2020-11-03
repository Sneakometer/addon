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

import de.hdskins.labymod.shared.actions.ActionConstants;
import de.hdskins.labymod.shared.actions.MarkedUserActionEntry;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.notify.NotificationUtil;
import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@ParametersAreNonnullByDefault
public class ReportUserActionEntry extends MarkedUserActionEntry implements ActionConstants {

    private final AddonContext addonContext;
    private final AtomicLong nextEnableTime = new AtomicLong();

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
        if (this.nextEnableTime.get() >= System.currentTimeMillis()) {
            LabyMod.getInstance().displayMessageInChat(this.addonContext.getTranslationRegistry().translateMessage(
                "user-skin-report-rate-limited",
                TimeUnit.MILLISECONDS.toSeconds(this.nextEnableTime.get() - System.currentTimeMillis())
            ));
            return;
        } else if (this.addonContext.getRateLimits().getReportRateLimit() > 0) {
            this.nextEnableTime.set(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.addonContext.getRateLimits().getReportRateLimit()));
        }

        AddonContext.ServerResult serverResult = this.addonContext.reportSkin(entityPlayer.getGameProfile().getId());
        if (serverResult.getExecutionStage() != AddonContext.ExecutionStage.EXECUTING) {
            LOGGER.debug("Unable to report hd skin of {}:{} with server result {}", entityPlayer.getName(), entityPlayer.getGameProfile().getId(), serverResult.getExecutionStage());
            NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("user-skin-report-failed-unknown"));
        } else {
            serverResult.getFuture().addListener(new ReportActionFutureListener(this.addonContext, entityPlayer));
        }
    }
}
