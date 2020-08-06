package de.hdskins.labymod.shared.language;

import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageManager {

    private LanguageManager() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, Properties> LOADED_LANGUAGES = new ConcurrentHashMap<>();
    private static final String DEFAULT_LANGUAGE = "en_US";

    private static MinecraftAdapter theMinecraftAdapter;
    private static String currentLanguageCode;

    public static void setMinecraftAdapter(MinecraftAdapter minecraftAdapter) {
        LanguageManager.theMinecraftAdapter = minecraftAdapter;
    }

    public static void updateLanguage() {
        String currentLanguage = theMinecraftAdapter == null ? null : theMinecraftAdapter.getCurrentLanguageCode();
        if (currentLanguage == null) {
            currentLanguage = DEFAULT_LANGUAGE;
        }

        if (LOADED_LANGUAGES.containsKey(currentLanguage)) {
            currentLanguageCode = currentLanguage;
            return;
        }

        Properties languageFile = loadLanguage(currentLanguage);
        if (languageFile == null) {
            languageFile = loadLanguage(DEFAULT_LANGUAGE);
        }

        if (languageFile != null) {
            LOADED_LANGUAGES.put(currentLanguage, languageFile);
        }

        currentLanguageCode = currentLanguage;
    }

    public static String getTranslation(String key, Object... objects) {
        return getTranslation(key, "<translation '" + key + "' missing>", objects);
    }

    public static String getTranslation(String key, String def, Object... objects) {
        if (currentLanguageCode == null) {
            updateLanguage();
        }

        String mcLanguage = theMinecraftAdapter == null ? null : theMinecraftAdapter.getCurrentLanguageCode();
        if (mcLanguage != null && currentLanguageCode != null && !mcLanguage.equals(currentLanguageCode)) {
            updateLanguage();
        }

        Properties properties = LOADED_LANGUAGES.get(currentLanguageCode == null ? DEFAULT_LANGUAGE : currentLanguageCode);
        if (properties == null) {
            LOADED_LANGUAGES.get(DEFAULT_LANGUAGE);
        }

        return properties == null ? def : MessageFormat.format(properties.getProperty(key, def), objects);
    }

    private static Properties loadLanguage(String lang) {
        if (lang.contains("_")) {
            lang = lang.split("_")[0];
        }

        try (InputStream inputStream = LanguageManager.class.getClassLoader().getResourceAsStream("lang/" + lang + ".properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (Throwable throwable) {
            return null;
        }
    }
}
