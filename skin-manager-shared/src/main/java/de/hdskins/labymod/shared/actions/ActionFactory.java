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

import de.hdskins.labymod.shared.actions.delete.DeleteUserActionEntry;
import de.hdskins.labymod.shared.actions.report.ReportUserActionEntry;
import de.hdskins.labymod.shared.actions.toggle.ToggleSkinUserActionEntry;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.role.UserRole;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public final class ActionFactory {

    private ActionFactory() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static List<MarkedUserActionEntry> bakeUserActionEntries(AddonContext addonContext) {
        MarkedUserActionEntry toggleSkinUserActionEntry = new ToggleSkinUserActionEntry(addonContext);
        if (addonContext.getRole().isHigherOrEqualThan(UserRole.STAFF)) {
            MarkedUserActionEntry deleteSkinUserActionEntry = new DeleteUserActionEntry(addonContext);
            return Arrays.asList(toggleSkinUserActionEntry, deleteSkinUserActionEntry);
        } else {
            MarkedUserActionEntry reportSkinUserActionEntry = new ReportUserActionEntry(addonContext);
            return Arrays.asList(reportSkinUserActionEntry, toggleSkinUserActionEntry);
        }
    }
}
