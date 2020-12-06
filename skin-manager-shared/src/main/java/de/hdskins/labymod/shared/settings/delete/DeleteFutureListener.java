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
package de.hdskins.labymod.shared.settings.delete;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.notify.NotificationUtil;
import de.hdskins.labymod.shared.utils.Constants;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketServerQueryResponse;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicBoolean;

@ParametersAreNonnullByDefault
public class DeleteFutureListener implements FutureListener<PacketBase>, Constants {

    private final AddonContext addonContext;
    private final AtomicBoolean stateListener;

    public DeleteFutureListener(AddonContext addonContext, AtomicBoolean stateListener) {
        this.addonContext = addonContext;
        this.stateListener = stateListener;
    }

    @Override
    public void nullResult() {
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("delete-skin-failed-unknown"));
        this.stateListener.set(false);
    }

    @Override
    public void nonNullResult(PacketBase packetBase) {
        if (packetBase instanceof PacketServerQueryResponse) {
            PacketServerQueryResponse response = (PacketServerQueryResponse) packetBase;
            if (response.isSuccess()) {
                NotificationUtil.notify(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("delete-skin-success"));
            } else {
                NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage(response.getReason(), response.getReason()));
            }
        } else {
            NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("delete-skin-failed-unknown"));
        }

        this.stateListener.set(false);
    }

    @Override
    public void cancelled() {
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("delete-skin-failed-unknown"));
        this.stateListener.set(false);
    }
}
