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

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.LocaleUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;
import java.util.Properties;

@ParametersAreNonnullByDefault
public class EmptyTranslationRegistry implements TranslationRegistry {

  protected static final TranslationRegistry EMPTY = new EmptyTranslationRegistry();

  private EmptyTranslationRegistry() {
  }

  @Override
  public void updateTranslation(String language, String translationKey, String message) {
  }

  @Nonnull
  @Override
  public String translateMessage(String translationKey, Object... replacements) {
    return this.translateMessageOrDefault(translationKey, "translation <" + translationKey + "> is missing", replacements);
  }

  @Nonnull
  @Override
  public String translateMessageOrDefault(String translationKey, String resultIfAbsent, Object... replacements) {
    return resultIfAbsent;
  }

  @Nonnull
  @Override
  public Locale getCurrentLocale() {
    return LocaleUtils.toLocale(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
  }

  @Nonnull
  @Override
  public String getCurrentLocaleKey() {
    return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().split("_")[0];
  }

  @Override
  public void reSyncLanguageCode() {
  }

  @Override
  public boolean isTranslationPresent(String language, String translationKey) {
    return false;
  }

  @Override
  public boolean loadLanguageFile(String languageKey, Properties languageFile) {
    return false;
  }

  @Override
  public void reset() {
  }
}
