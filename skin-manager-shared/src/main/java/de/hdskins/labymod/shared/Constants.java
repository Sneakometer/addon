/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 - 2021 HD-Skins <https://github.com/HDSkins>
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
package de.hdskins.labymod.shared;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.hdskins.labymod.shared.eventbus.EventBus;
import de.hdskins.labymod.shared.eventbus.defaults.DefaultEventBus;
import de.hdskins.labymod.shared.utils.LabyModUtils;
import net.labymod.addon.About;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.main.Source;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public interface Constants {
  String SUCCESS = "§a§l✔";
  String FAILURE = "§c§l✖";
  String SPACE = " ";
  Gson GSON = new Gson();
  JsonParser JSON_PARSER = new JsonParser();
  EventBus EVENT_BUS = new DefaultEventBus();
  ExecutorService EXECUTOR = Executors.newCachedThreadPool();
  AtomicReference<About> ABOUT = new AtomicReference<>();
  AtomicReference<AddonInfo> ADDON_INFO = new AtomicReference<>();

  @Nonnull
  static String getUserAgent() {
    return "HDSkins v" + getAddonInfo().getVersion() + " on LabyMod v" + Source.ABOUT_VERSION + " on mc " + Source.ABOUT_MC_VERSION;
  }

  @Nonnull
  static AddonInfo getAddonInfo() {
    AddonInfo addonInfo = ADDON_INFO.get();
    if (addonInfo == null) {
      ADDON_INFO.set(addonInfo = LabyModUtils.getAddonInfo(ABOUT.get()));
    }
    return addonInfo;
  }
}
