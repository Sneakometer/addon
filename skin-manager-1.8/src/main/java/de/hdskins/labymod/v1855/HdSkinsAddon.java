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

import de.hdskins.labymod.shared.ReflectionUtils;
import de.hdskins.labymod.shared.addon.AddonContextLoader;
import de.hdskins.labymod.shared.texture.HDSkinManager;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class HdSkinsAddon extends LabyModAddon implements Consumer<String> {

    @Override
    public void onEnable() {
        System.out.println("\n        __  ______  _____ __   _\n" +
            "       / / / / __ \\/ ___// /__(_)___  _____\n" +
            "      / /_/ / / / /\\__ \\/ //_/ / __ \\/ ___/\n" +
            "     / __  / /_/ /___/ / ,< / / / / (__  )\n" +
            "    /_/ /_/_____//____/_/|_/_/_/ /_/____/\n" +
            "\n" +
            "          Copyright (c) 2020 HDSkins\n" +
            "   Support Discord: https://discord.gg/KN8rDZJ");
    }

    @Override
    public void loadConfig() {
    }

    @Override
    public void init(String addonName, UUID uuid) {
        super.init(addonName, uuid);
        AddonContextLoader.initAddon(this).thenAcceptAsync(context -> {
            File skinCacheDir = ReflectionUtils.get(File.class, Minecraft.class, Minecraft.getMinecraft(), "c", "skinCacheDir", "field_152796_d");
            Objects.requireNonNull(skinCacheDir, "Unable to load skin cache dir correctly!");
            ReflectionUtils.set(Minecraft.class, Minecraft.getMinecraft(), new HDSkinManager(context, skinCacheDir.toPath()), "aL", "skinManager", "field_152350_aA");
        });
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
    }

    @Override
    public void accept(String s) {
    }
}
