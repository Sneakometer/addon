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
package de.hdskins.labymod.shared.utils;

import de.hdskins.labymod.shared.Constants;
import org.apache.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UniqueIdUtils {

  private static final UUID RUNNING = new UUID(0, 0);
  private static final Map<String, UUID> CACHE = new ConcurrentHashMap<>();
  private static final String LOOKUP_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

  private UniqueIdUtils() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  public static UUID getOrLookupAndCache(@Nonnull String name) {
    final UUID result = CACHE.putIfAbsent(name, RUNNING);
    if (result == null) {
      doLookup(name);
    }
    return result == RUNNING ? null : result;
  }

  private static void doLookup(@Nonnull String name) {
    Constants.EXECUTOR.execute(() -> {
      final Response response = HttpUtils.doGet(String.format(LOOKUP_URL, name), HttpStatus.SC_OK, httpResponse -> {
        try (InputStreamReader reader = new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8)) {
          return GameProfileUtils.GSON.fromJson(reader, Response.class);
        }
      }, null);

      if (response != null && response.id != null) {
        CACHE.put(name, response.id);
      } else {
        CACHE.remove(name);
      }
    });
  }

  private static final class Response {
    private UUID id;
  }
}
