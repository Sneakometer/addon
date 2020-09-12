package de.hdskins.labymod.v18;

import com.mojang.authlib.exceptions.AuthenticationException;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.shared.utils.ServerHelper;
import de.hdskins.labymod.v18.listener.TickListener;
import de.hdskins.labymod.v18.manager.HDSkinManager;
import de.hdskins.labymod.v18.settings.V18SettingsManager;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.Session;

import java.util.List;
import java.util.Optional;

public class V18MinecraftAdapter implements MinecraftAdapter {

    private ConfigObject config;
    private final V18SettingsManager settingsManager = new V18SettingsManager();

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

    @Override
    public boolean authorize() {
        Session session = Minecraft.getMinecraft().getSession();
        if (session == null) {
            return false;
        }

        try {
            Minecraft.getMinecraft().getSessionService().joinServer(session.getProfile(), session.getToken(), Constants.SERVER_ID);
            return true;
        } catch (AuthenticationException exception) {
            exception.printStackTrace();
            return false;
        }
    }

}
