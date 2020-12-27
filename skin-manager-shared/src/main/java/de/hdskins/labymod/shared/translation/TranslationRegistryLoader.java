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
package de.hdskins.labymod.shared.translation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ParametersAreNonnullByDefault
public final class TranslationRegistryLoader {

  private static final Logger LOGGER = LogManager.getLogger(TranslationRegistryLoader.class);

  private TranslationRegistryLoader() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public static TranslationRegistry buildInternalTranslationRegistry() {
    try (InputStream inputStream = TranslationRegistryLoader.class.getClassLoader().getResourceAsStream("languages")) {
      if (inputStream == null) {
        return TranslationRegistry.empty();
      }

      Map<String, Properties> languages = new HashMap<>();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("# ") || line.trim().isEmpty() || !line.contains(":")) {
            continue;
          }

          String[] parts = line.split(":");
          if (parts.length == 2) {
            openStreamOrLog(parts[1], stream -> {
              Properties properties = new Properties();
              properties.load(stream);
              languages.put(parts[0], properties);
            });
          }
        }
      }

      return TranslationRegistry.fromMap(languages);
    } catch (IOException exception) {
      LOGGER.debug("Unable to load language files", exception);
      return TranslationRegistry.empty();
    }
  }

  private static void openStreamOrLog(String file, IOExceptionConsumer<InputStream> consumer) {
    try (InputStream stream = TranslationRegistryLoader.class.getClassLoader().getResourceAsStream("lang/" + file)) {
      if (stream != null) {
        consumer.accept(stream);
      }
    } catch (IOException exception) {
      LOGGER.debug("Unable to load language file {}", file, exception);
    }
  }

  @FunctionalInterface
  private interface IOExceptionConsumer<T> {
    void accept(@Nonnull T t) throws IOException;
  }
}
