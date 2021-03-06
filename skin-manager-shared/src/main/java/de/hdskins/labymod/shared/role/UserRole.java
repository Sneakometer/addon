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
package de.hdskins.labymod.shared.role;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public enum UserRole {

  ADMIN,
  STAFF,
  VIP,
  USER;

  private static final UserRole[] VALUES = values(); // prevent copy

  public static UserRole roleFromOrdinalIndex(byte index) {
    return index >= 0 && index < VALUES.length ? VALUES[index] : USER;
  }

  public boolean isHigherOrEqualThan(UserRole other) {
    return super.ordinal() <= other.ordinal();
  }
}
