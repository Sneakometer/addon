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
import de.hdskins.labymod.shared.settings.countdown.ButtonCountdownElementNameChanger;
import de.hdskins.labymod.shared.settings.countdown.SettingsCountdownRegistry;
import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import de.hdskins.labymod.shared.Constants;
import net.labymod.utils.Consumer;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeleteButtonClickHandler implements Consumer<ButtonElement>, Constants {

    private final AtomicBoolean actionRunning = new AtomicBoolean();
    private final DeleteFutureListener deleteFutureListener;
    private final AddonContext addonContext;

    public DeleteButtonClickHandler(AddonContext addonContext) {
        this.deleteFutureListener = new DeleteFutureListener(addonContext, this.actionRunning);
        this.addonContext = addonContext;
    }

    @Override
    public void accept(ButtonElement buttonElement) {
        if (!this.actionRunning.getAndSet(true)) {
            AddonContext.ServerResult serverResult = this.addonContext.deleteSkin();
            if (serverResult.getExecutionStage() != AddonContext.ExecutionStage.EXECUTING) {
                NotificationUtil.notify(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("delete-skin-success"));
            } else {
                serverResult.getFuture().addListener(this.deleteFutureListener);
            }

            if (this.addonContext.getRateLimits().getDeleteRateLimit() > 0) {
                SettingsCountdownRegistry.registerTask(
                    new ButtonCountdownElementNameChanger(buttonElement),
                    this.addonContext.getRateLimits().getDeleteRateLimit()
                );
            }
        }
    }
}
