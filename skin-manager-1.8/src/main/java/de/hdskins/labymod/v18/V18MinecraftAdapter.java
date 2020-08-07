package de.hdskins.labymod.v18;

import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.v18.settings.V18SettingsManager;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.Language;

import java.util.List;
import java.util.Optional;

public class V18MinecraftAdapter implements MinecraftAdapter {

    private final V18SettingsManager settingsManager = new V18SettingsManager();

    @Override
    public String getSessionId() {
        return Minecraft.getMinecraft().getSession().getToken();
    }

    @Override
    public void fillSettings(List<SettingsElement> list, ConfigObject object, boolean slim) {
        if (this.settingsManager.shouldRedraw() || !list.isEmpty()) {
            this.settingsManager.redraw();
        } else {
            this.settingsManager.draw(this, list, object, slim);
        }
    }

    @Override
    public void displayMessageInChat(String message) {
        LabyMod.getInstance().displayMessageInChat(message);
    }

    @Override
    public void changeToIngame() {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(null));
    }

    @Override
    public Optional<PlayerProfile> resolveUniqueId(String name) {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) {
            return Optional.empty();
        }

        NetworkPlayerInfo playerInfo = netHandler.getPlayerInfo(name);
        if (playerInfo == null) {
            return Optional.empty();
        }

        return Optional.of(new PlayerProfile(playerInfo.getGameProfile().getName(), playerInfo.getGameProfile().getId()));
    }

    @Override
    public String getCurrentLanguageCode() {
        Language language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
        return language == null ? null : language.getLanguageCode();
    }
}
