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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@ParametersAreNonnullByDefault
public interface TranslationRegistry {

    static TranslationRegistry empty() {
        return EmptyTranslationRegistry.EMPTY;
    }

    static TranslationRegistry fromMap(Map<String, Properties> loadedLanguages) {
        return new DefaultTranslationRegistry(loadedLanguages);
    }

    void updateTranslation(String language, String translationKey, String message);

    @Nonnull
    String translateMessage(String translationKey, Object... replacements);

    @Nonnull
    String translateMessageOrDefault(String translationKey, String resultIfAbsent, Object... replacements);

    @Nonnull
    Locale getCurrentLocale();

    @Nonnull
    String getCurrentLocaleKey();

    void reSyncLanguageCode();

    boolean isTranslationPresent(String language, String translationKey);

    boolean loadLanguageFile(String languageKey, Properties languageFile);
}
