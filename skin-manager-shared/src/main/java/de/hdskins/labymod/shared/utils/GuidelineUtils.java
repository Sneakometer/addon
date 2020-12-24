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

import de.hdskins.labymod.shared.Constants;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

public final class GuidelineUtils {

  private static Collection<String> cached;

  private GuidelineUtils() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public static Collection<String> readGuidelines(@Nonnull String url) {
    if (cached == null) {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", Constants.getUserAgent());
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        if (connection.getResponseCode() == 200) {
          try (final InputStream stream = connection.getInputStream()) {
            cached = IOUtils.readLines(stream, StandardCharsets.UTF_8);
          }
        } else {
          cached = Collections.emptyList();
        }
        connection.disconnect();
      } catch (Throwable exception) {
        cached = Collections.emptyList();
      }
    }
    return cached;
  }
}
