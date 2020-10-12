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
package de.hdskins.labymod.shared.translation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ParametersAreNonnullByDefault
public final class TranslationRegistryLoader {

    private static final Logger LOGGER = LogManager.getLogger(TranslationRegistryLoader.class);

    private TranslationRegistryLoader() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static TranslationRegistry buildInternalTranslationRegistry() {
        CodeSource codeSource = TranslationRegistryLoader.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            LOGGER.debug("Unable to load language files because code source of class is null");
            return TranslationRegistry.empty();
        }

        Map<String, Properties> languageFiles = new ConcurrentHashMap<>();
        try (ZipFile zipFile = new ZipFile(new File(codeSource.getLocation().toURI()))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                final String name = zipEntry.getName();
                if (name.startsWith("lang/") && name.endsWith(".properties")) {
                    try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                        Properties properties = new Properties();
                        properties.load(inputStream);
                        languageFiles.put(StringUtils.removeEnd(name, ".properties"), properties);
                    } catch (IOException exception) {
                        LOGGER.debug("Unable to load language file {}", name, exception);
                    }
                }
            }
        } catch (URISyntaxException | IOException exception) {
            LOGGER.debug("Unable to load language files", exception);
        }

        String result = languageFiles.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue()).collect(Collectors.joining(", "));
        LOGGER.debug("Loaded {} language files: {}", languageFiles.size(), result);
        return languageFiles.isEmpty() ? TranslationRegistry.empty() : TranslationRegistry.fromMap(languageFiles);
    }
}
