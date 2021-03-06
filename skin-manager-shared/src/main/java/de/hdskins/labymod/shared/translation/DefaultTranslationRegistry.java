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

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.event.TranslationLanguageCodeChangeEvent;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.LocaleUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@ParametersAreNonnullByDefault
public class DefaultTranslationRegistry implements TranslationRegistry {

  private final Map<String, Properties> originalLanguageFiles;
  private final Map<String, Properties> loadedLanguageFiles;
  private String currentLocale;

  protected DefaultTranslationRegistry(Map<String, Properties> loadedLanguageFiles) {
    this.originalLanguageFiles = new HashMap<>(loadedLanguageFiles);
    this.loadedLanguageFiles = loadedLanguageFiles;
    this.reSyncLanguageCode();
  }

  @Override
  public void updateTranslation(String language, String translationKey, String message) {
    final Properties properties = this.loadedLanguageFiles.get(language);
    if (properties != null) {
      properties.setProperty(translationKey, message);
    }
  }

  @Nonnull
  @Override
  public String translateMessage(String translationKey, Object... replacements) {
    return this.translateMessageOrDefault(translationKey, "translation <" + translationKey + "> is missing", replacements);
  }

  @Nonnull
  @Override
  public String translateMessageOrDefault(String translationKey, String resultIfAbsent, Object... replacements) {
    this.reSyncLanguageCode();
    Properties source = this.loadedLanguageFiles.get(this.currentLocale);
    if (source == null) {
      // Try to use english as fallback
      source = this.loadedLanguageFiles.get("en");
    }

    return source == null ? MessageFormat.format(resultIfAbsent, replacements) : MessageFormat.format(source.getProperty(translationKey, resultIfAbsent), replacements);
  }

  @Nonnull
  @Override
  public Locale getCurrentLocale() {
    this.reSyncLanguageCode();
    return LocaleUtils.toLocale(this.currentLocale);
  }

  @Nonnull
  @Override
  public String getCurrentLocaleKey() {
    this.reSyncLanguageCode();
    return this.currentLocale;
  }

  @Override
  public void reSyncLanguageCode() {
    String chosenLocale = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().split("_")[0];
    if (this.currentLocale == null || !this.currentLocale.equals(chosenLocale)) {
      this.currentLocale = chosenLocale;
      Constants.EVENT_BUS.postReported(TranslationLanguageCodeChangeEvent.EVENT);
    }
  }

  @Override
  public boolean isTranslationPresent(String language, String translationKey) {
    this.reSyncLanguageCode();
    Properties source = this.loadedLanguageFiles.get(language);
    return source != null && source.containsKey(translationKey);
  }

  @Override
  public boolean loadLanguageFile(String languageKey, Properties languageFile) {
    return this.loadedLanguageFiles.putIfAbsent(languageKey, languageFile) == null;
  }

  @Override
  public void reset() {
    this.loadedLanguageFiles.clear();
    this.loadedLanguageFiles.putAll(this.originalLanguageFiles);
  }
}
