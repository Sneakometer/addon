package de.hdskins.labymod.v112;

import de.hdskins.labymod.shared.callbacks.SettingsStringCallback;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.elements.ButtonElementHandler;
import de.hdskins.labymod.shared.gui.ButtonElement;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.v112.gui.V112ButtonElement;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;

import java.util.List;

public class V112MinecraftAdapter implements MinecraftAdapter {

    @Override
    public String getSessionId() {
        return Minecraft.getMinecraft().getSession().getSessionID();
    }

    @Override
    public void fillSettings(List<SettingsElement> list, ConfigObject object, LabyModAddon addon) {
        SettingsElement serverUrlElement = new StringElement(
                "Server-Url", addon, new ControlElement.IconData(Material.DIAMOND_SWORD), "url", object.getServerUrl()
        ).addCallback(new SettingsStringCallback(object::setServerUrl));
        list.add(serverUrlElement);

        SettingsElement tokenElement = new StringElement(
                "Token", addon, new ControlElement.IconData(Material.GOLDEN_APPLE), "token", object.getToken()
        ).addCallback(new SettingsStringCallback(object::setToken));
        list.add(tokenElement);

        ButtonElement controlElement = new V112ButtonElement("Change skin", new ControlElement.IconData(Material.PAINTING), null);
        controlElement.setDescriptionText("Skins must not contain right-wing, narcissistic, offensive or sexual content.");
        controlElement.setClickListener(new ButtonElementHandler());
        list.add(controlElement);
    }
}
