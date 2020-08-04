package de.hdskins.labymod.shared.minecraft;

import de.hdskins.labymod.shared.config.ConfigObject;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

public interface MinecraftAdapter {

    String getSessionId();

    void fillSettings(List<SettingsElement> list, ConfigObject object);

    void drawString(String text, double x, double y, double size);

    int getWidth();
}
