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
package de.hdskins.labymod.v1855;

import de.hdskins.labymod.shared.utils.ReflectionUtils;
import de.hdskins.labymod.shared.addon.AddonContextLoader;
import de.hdskins.labymod.shared.addon.laby.LabyModAddonBase;
import de.hdskins.labymod.shared.texture.HDSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

public class HdSkinsAddon extends LabyModAddonBase {

    @Override
    protected void createAddonContext() {
        AddonContextLoader.initAddon(this).thenAcceptAsync(context -> {
            // Initialize screen factory
            AcceptRejectGuiScreenImpl.init();
            // assets directory
            File assetsDir = ReflectionUtils.get(File.class, Minecraft.class, Minecraft.getMinecraft(), "fileAssets", "field_110446_Y", "ak");
            Objects.requireNonNull(assetsDir, "Unable to assets dir correctly!");
            // Network player info bridge
            Field playerTexturesLoaded = ReflectionUtils.getFieldByNames(NetworkPlayerInfo.class, "playerTexturesLoaded", "field_178864_d", "d");
            Objects.requireNonNull(playerTexturesLoaded, "Unable to find playerTexturesLoaded boolean");

            ReflectionUtils.set(Minecraft.class, Minecraft.getMinecraft(), new HDSkinManager(
                context,
                assetsDir,
                uniqueId -> {
                    NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
                    if (netHandler != null) {
                        NetworkPlayerInfo playerInfo = netHandler.getPlayerInfo(uniqueId);
                        if (playerInfo != null) {
                            ReflectionUtils.set(playerInfo, Boolean.FALSE, playerTexturesLoaded);
                        }
                    }
                }
            ), "skinManager", "field_152350_aA", "aL");
        });
    }
}
