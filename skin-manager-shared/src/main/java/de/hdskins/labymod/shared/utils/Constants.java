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
package de.hdskins.labymod.shared.utils;

import de.hdskins.labymod.shared.eventbus.EventBus;
import de.hdskins.labymod.shared.eventbus.defaults.DefaultEventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Constants {
    String SUCCESS = "§a§l✔";
    String FAILURE = "§c§l✖";
    EventBus EVENT_BUS = new DefaultEventBus();
    ExecutorService EXECUTOR = Executors.newCachedThreadPool();
}
