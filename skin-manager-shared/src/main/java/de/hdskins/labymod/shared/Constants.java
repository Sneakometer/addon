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
package de.hdskins.labymod.shared;

import de.hdskins.labymod.shared.eventbus.EventBus;
import de.hdskins.labymod.shared.eventbus.defaults.DefaultEventBus;
import net.labymod.main.Source;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public interface Constants {
  String SUCCESS = "§a§l✔";
  String FAILURE = "§c§l✖";
  String SPACE = " ";
  EventBus EVENT_BUS = new DefaultEventBus();
  ExecutorService EXECUTOR = Executors.newCachedThreadPool();
  AtomicReference<String> ADDON_VERSION = new AtomicReference<>();

  @Nonnull
  static String getUserAgent() {
    return "HDSkins v" + ADDON_VERSION.get() + " on LabyMod v" + Source.ABOUT_VERSION + " on mc " + Source.ABOUT_MC_VERSION;
  }
}
