package de.hdskins.labymod.v18;

import de.hdskins.labymod.shared.callbacks.SettingsStringCallback;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.elements.UploadFileButtonClickHandler;
import de.hdskins.labymod.shared.gui.ButtonElement;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.v18.gui.V18ButtonElement;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;

import java.util.List;

public class V18MinecraftAdapter implements MinecraftAdapter {

    @Override
    public String getSessionId() {
        return Minecraft.getMinecraft().getSession().getToken();
    }

    @Override
    public void fillSettings(List<SettingsElement> list, ConfigObject object) {
        SettingsElement serverUrlElement = new StringElement(
                "Server-Url", new ControlElement.IconData(Material.DIAMOND_SWORD),
                object.getServerUrl(), new SettingsStringCallback(object::setServerUrl)
        );
        serverUrlElement.setDescriptionText("The server url to use to download the skins from/upload skins to");
        list.add(serverUrlElement);

        SettingsElement tokenElement = new StringElement(
                "Token", new ControlElement.IconData(Material.GOLDEN_APPLE),
                object.getToken(), new SettingsStringCallback(object::setToken)
        );
        tokenElement.setDescriptionText("The admin token to bypass rate limits if available");
        list.add(tokenElement);

        ButtonElement controlElement = new V18ButtonElement("Change skin", new ControlElement.IconData(Material.PAINTING), "Click here");
        controlElement.setDescriptionText("Skins must not contain right-wing, narcissistic, offensive or sexual content.");
        controlElement.setClickListener(new UploadFileButtonClickHandler(this, object));
        list.add(controlElement);
    }

    @Override
    public void drawString(String text, double x, double y, double size) {
        LabyMod.getInstance().getDrawUtils().drawCenteredString(text, x, y, size);
    }

    @Override
    public int getWidth() {
        return LabyMod.getInstance().getDrawUtils().getWidth();
    }
}
