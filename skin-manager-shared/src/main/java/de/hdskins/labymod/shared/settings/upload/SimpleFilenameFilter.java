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
package de.hdskins.labymod.shared.settings.upload;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

public class SimpleFilenameFilter implements FilenameFilter {

  private final Set<String> acceptedExtensions;

  public SimpleFilenameFilter(@Nonnull Set<String> acceptedExtensions) {
    this.acceptedExtensions = acceptedExtensions;
  }

  @Override
  public boolean accept(@Nonnull File dir, @Nonnull String name) {
    final int index = name.lastIndexOf('.');
    if (index == -1) {
      // Include files without an extension
      return true;
    }
    final String extension = name.substring(index + 1);
    return this.acceptedExtensions.contains(extension.toLowerCase());
  }
}
