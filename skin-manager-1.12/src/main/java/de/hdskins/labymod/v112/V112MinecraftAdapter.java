package de.hdskins.labymod.v112;

import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.shared.utils.ServerHelper;
import de.hdskins.labymod.v112.listener.TickListener;
import de.hdskins.labymod.v112.manager.HDSkinManager;
import de.hdskins.labymod.v112.settings.V112SettingsManager;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.SkinManager;

import java.util.List;
import java.util.Optional;

public class V112MinecraftAdapter implements MinecraftAdapter {

    private final V112SettingsManager settingsManager = new V112SettingsManager();

    private ConfigObject config;

    @Override
    public String getSessionId() {
        return Minecraft.getMinecraft().getSession().getToken();
    }

    @Override
    public void fillSettings(List<SettingsElement> list, ConfigObject object, boolean slim) {
        this.config = object;
        if (this.settingsManager.shouldRedraw() || !list.isEmpty()) {
            this.settingsManager.redraw();
        } else {
            this.settingsManager.draw(this, list, object, slim);
        }

        LabyMod.getInstance().getLabyModAPI().registerForgeListener(new TickListener(this));
    }

    @Override
    public void updateSlimState() {
        this.settingsManager.setSlim(ServerHelper.isSlim(this.config));
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
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getConnection();
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
        return language.getLanguageCode();
    }

    @Override
    public ConfigObject getConfig() {
        return this.config;
    }

    @Override
    public void invalidateSkinCache() {
        SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
        if (skinManager instanceof HDSkinManager) {
            ((HDSkinManager) skinManager).invalidateCache();
        }
    }

}
