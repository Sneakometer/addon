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
package de.hdskins.labymod.shared.settings;

import net.labymod.addon.About;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.settings.elements.SettingsElement;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public final class SettingInvoker {

    private static About about;
    private static List<SettingsElement> loadedSettings;

    private SettingInvoker() {
        throw new UnsupportedOperationException();
    }

    public static void setAbout(About about) {
        SettingInvoker.about = about;
    }

    @Nonnull
    public static List<SettingsElement> getLoadedSettings() {
        if (loadedSettings == null) {
            loadedSettings = AddonInfoManager.getInstance().getAddonInfoMap().get(about.uuid).getAddonElement().getSubSettings();
        }

        return loadedSettings;
    }

    public static void addSettingsElement(SettingsElement settingsElement) {
        getLoadedSettings().add(settingsElement);
    }

    public static void unloadSettingElements() {
        getLoadedSettings().clear();
    }
}
