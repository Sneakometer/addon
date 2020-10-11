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

import de.hdskins.labymod.shared.config.AddonConfig;
import de.hdskins.protocol.client.NetworkClient;
import net.labymod.api.LabyModAddon;

public class AddonContext {

    private final NetworkClient networkClient;
    private final LabyModAddon labyModAddon;
    private final AddonConfig addonConfig;

    public AddonContext(NetworkClient networkClient, LabyModAddon labyModAddon, AddonConfig addonConfig) {
        this.networkClient = networkClient;
        this.labyModAddon = labyModAddon;
        this.addonConfig = addonConfig;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public LabyModAddon getLabyModAddon() {
        return labyModAddon;
    }

    public AddonConfig getAddonConfig() {
        return addonConfig;
    }
}
