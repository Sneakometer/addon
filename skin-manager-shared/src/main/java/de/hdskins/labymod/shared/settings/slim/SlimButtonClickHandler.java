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
package de.hdskins.labymod.shared.settings.slim;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.notify.NotificationUtil;
import de.hdskins.labymod.shared.utils.Constants;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class SlimButtonClickHandler implements Function<Boolean, CompletableFuture<Boolean>>, Constants {

    private final AddonContext addonContext;

    public SlimButtonClickHandler(AddonContext addonContext) {
        this.addonContext = addonContext;
    }

    @Override
    public CompletableFuture<Boolean> apply(Boolean aBoolean) {
        AddonContext.ServerResult serverResult = this.addonContext.updateSlim(aBoolean);
        if (serverResult.getExecutionStage() != AddonContext.ExecutionStage.EXECUTING) {
            NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("slim-toggle-failed-unknown"));
            return CompletableFuture.completedFuture(!aBoolean);
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        serverResult.getFuture().addListener(new SlimFutureListener(aBoolean, this.addonContext, future));
        return future;
    }
}
