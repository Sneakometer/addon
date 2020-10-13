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
package de.hdskins.labymod.shared.notify;

import net.labymod.main.LabyMod;
import net.labymod.utils.ModColor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NotificationUtil {

    private NotificationUtil() {
        throw new UnsupportedOperationException();
    }

    public static void notify(String title, String description) {
        LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(title, description);
    }

    public static void notifyUsingLabyConnectSetting(String title, String description) {
        switch (LabyMod.getInstance().getLabyConnect().getAlertDisplayType()) {
            case CHAT:
                LabyMod.getInstance().displayMessageInChat(ModColor.cl('e') + title + ModColor.cl('7') + ": " + ModColor.cl('f') + description);
                break;
            case ACHIEVEMENT:
                LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(title, description);
                break;
            default:
                break;
        }
    }
}
