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
package de.hdskins.labymod.shared.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public interface AddonConfig extends Serializable {

    String getServerHost();

    void setServerHost(String serverHost);

    int getServerPort();

    void setServerPort(int serverPort);

    long getFirstReconnectInterval();

    void setFirstReconnectInterval(long firstReconnectInterval);

    long getReconnectInterval();

    void setReconnectInterval(long reconnectInterval);

    boolean showSkinsOfOtherPlayers();

    void setShowSkinsOfOtherPlayers(boolean showSkinsOfOtherPlayers);

    boolean isSlim();

    void setSlim(boolean slim);

    Collection<UUID> getDisabledSkins();

    void removeAllDisabledSkins();

    void disableSkin(UUID playerUniqueId);

    void enableSkin(UUID playerUniqueId);

    boolean isSkinDisabled(UUID playerUniqueId);
}
