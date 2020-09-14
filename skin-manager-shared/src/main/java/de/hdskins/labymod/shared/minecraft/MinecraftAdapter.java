package de.hdskins.labymod.shared.minecraft;

import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;
import java.util.Optional;

public interface MinecraftAdapter {

    void fillSettings(List<SettingsElement> list, ConfigObject object, boolean slim);

    void updateSlimState();

    void displayMessageInChat(String message);

    void changeToIngame();

    Optional<PlayerProfile> resolveUniqueId(String name);

    String getCurrentLanguageCode();

    ConfigObject getConfig();

    void invalidateSkinCache();

    void updateSelfSkin();

    boolean authorize();

    int getWindowHeight();

    int getWindowWidth();

    void renderPlayer(int x, int y, int mouseX, int mouseY, int size, int rotation);

}