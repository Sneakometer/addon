package de.hdskins.labymod.shared.minecraft;

import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;
import java.util.Optional;

public interface MinecraftAdapter {

    String getSessionId();

    void fillSettings(List<SettingsElement> list, ConfigObject object, boolean slim);

    void displayMessageInChat(String message);

    void changeToIngame();

    Optional<PlayerProfile> resolveUniqueId(String name);
}
