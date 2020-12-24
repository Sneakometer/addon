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

import de.hdskins.labymod.shared.utils.ReflectionUtils;
import net.labymod.main.LabyMod;
import net.labymod.user.gui.UserActionGui;
import net.labymod.user.util.UserActionEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public final class ActionInvoker {

    private static final List<UserActionEntry> REGISTERED_USER_ACTION_ENTRIES;

    static {
        REGISTERED_USER_ACTION_ENTRIES = ReflectionUtils.get(List.class, UserActionGui.class, LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
    }

    private ActionInvoker() {
        throw new UnsupportedOperationException();
    }

    public static void addUserActionEntry(UserActionEntry userActionEntry) {
        REGISTERED_USER_ACTION_ENTRIES.add(userActionEntry);
    }

    public static void unregisterMarkedEntries() {
        REGISTERED_USER_ACTION_ENTRIES.removeIf(entry -> entry instanceof MarkedUserActionEntry);
    }
}
