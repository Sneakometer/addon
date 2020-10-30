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
package de.hdskins.labymod.v2512;

import de.hdskins.labymod.shared.ReflectionUtils;
import de.hdskins.labymod.shared.addon.AddonContextLoader;
import de.hdskins.labymod.shared.addon.laby.LabyModAddonBase;
import de.hdskins.labymod.shared.texture.HDSkinManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.Objects;

public class HdSkinsAddon extends LabyModAddonBase {

    @Override
    protected void createAddonContext() {
        AddonContextLoader.initAddon(this).thenAcceptAsync(context -> {
            File skinCacheDir = ReflectionUtils.get(File.class, Minecraft.class, Minecraft.getMinecraft(), "skinCacheDir", "field_152796_d", "c");
            Objects.requireNonNull(skinCacheDir, "Unable to load skin cache dir correctly!");
            //TODO: ReflectionUtils.set(Minecraft.class, Minecraft.getMinecraft(), new HDSkinManager(context, skinCacheDir.toPath()), "aP", "skinManager", "field_152350_aA");
        });
    }
}
