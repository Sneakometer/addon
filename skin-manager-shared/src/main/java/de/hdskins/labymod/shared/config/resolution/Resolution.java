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
package de.hdskins.labymod.shared.config.resolution;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public enum Resolution {

  RESOLUTION_128x64(128, 64),
  RESOLUTION_128x128(128, 128),
  RESOLUTION_256x128(256, 128),
  RESOLUTION_256x256(256, 256),
  RESOLUTION_512x256(512, 256),
  RESOLUTION_512x512(512, 512),
  RESOLUTION_1024x512(1024, 512),
  RESOLUTION_1024x1024(1024, 1024),
  RESOLUTION_2048x1024(2048, 1024),
  RESOLUTION_2048x2048(2048, 2048),
  RESOLUTION_ALL(0, 0);

  public static final Resolution[] VALUES = values(); // prevent copy

  private final int width;
  private final int height;

  Resolution(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Nonnull
  public static Optional<Resolution> byName(String name) {
    for (Resolution value : VALUES) {
      if (value.name().equalsIgnoreCase(name)) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }
}
