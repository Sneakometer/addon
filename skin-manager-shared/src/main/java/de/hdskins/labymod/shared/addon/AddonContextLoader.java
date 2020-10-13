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
package de.hdskins.labymod.shared.addon;

import de.hdskins.labymod.shared.actions.ActionInvoker;
import de.hdskins.labymod.shared.actions.delete.DeleteUserActionEntry;
import de.hdskins.labymod.shared.actions.report.ReportUserActionEntry;
import de.hdskins.labymod.shared.actions.toggle.ToggleSkinUserActionEntry;
import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.config.AddonConfig;
import de.hdskins.labymod.shared.config.JsonAddonConfig;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.settings.SettingInvoker;
import de.hdskins.labymod.shared.settings.SettingsFactory;
import de.hdskins.labymod.shared.translation.TranslationRegistry;
import de.hdskins.labymod.shared.translation.TranslationRegistryLoader;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public final class AddonContextLoader {

    private static final Logger LOGGER = LogManager.getLogger(AddonContextLoader.class);

    private AddonContextLoader() {
        throw new UnsupportedOperationException();
    }

    public static CompletableFuture<AddonContext> initAddon(LabyModAddon addon) {
        AddonConfig addonConfig = JsonAddonConfig.load(addon);
        LOGGER.debug("Loaded addon config: {} with hash: {}", addonConfig.toString(), addonConfig.hashCode());
        return BackendUtils.connectToServer(addonConfig).thenApplyAsync(networkClient -> {
            TranslationRegistry translationRegistry = TranslationRegistryLoader.buildInternalTranslationRegistry();
            return new AddonContext(addonConfig, addon, networkClient, translationRegistry);
        }).thenApply(addonContext -> {
            ActionInvoker.addUserActionEntry(new ReportUserActionEntry(addonContext));
            ActionInvoker.addUserActionEntry(new ToggleSkinUserActionEntry(addonContext));
            if (addonContext.getRole().isHigherOrEqualThan(UserRole.STAFF)) {
                ActionInvoker.addUserActionEntry(new DeleteUserActionEntry(addonContext));
            }
            return addonContext;
        }).thenApply(addonContext -> {
            for (SettingsElement element : SettingsFactory.bakeSettings(addonContext)) {
                SettingInvoker.addSettingsElement(element);
            }
            return addonContext;
        });
    }
}
