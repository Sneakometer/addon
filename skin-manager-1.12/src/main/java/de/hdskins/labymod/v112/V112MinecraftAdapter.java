package de.hdskins.labymod.v112;

import com.google.gson.JsonElement;
import com.mojang.authlib.exceptions.AuthenticationException;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.shared.utils.ServerHelper;
import de.hdskins.labymod.v112.listener.TickListener;
import de.hdskins.labymod.v112.manager.HDSkinManager;
import de.hdskins.labymod.v112.settings.V112SettingsManager;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.JsonParse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class V112MinecraftAdapter implements MinecraftAdapter {

    private static final Field TEXTURES_LOADED_FIELD;

    static {
        Field texturesLoadedField = null;

        try {
            texturesLoadedField = ReflectionHelper.findField(NetworkPlayerInfo.class, "playerTexturesLoaded", "e", "field_178864_d");
            texturesLoadedField.setAccessible(true);
        } catch (ReflectionHelper.UnableToFindFieldException exception) {
        }

        TEXTURES_LOADED_FIELD = texturesLoadedField;
    }

    private final V112SettingsManager settingsManager = new V112SettingsManager();

    private ConfigObject config;

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
        this.clearSkinCache();

        if (Minecraft.getMinecraft().getConnection() != null) {
            for (NetworkPlayerInfo info : Minecraft.getMinecraft().getConnection().getPlayerInfoMap()) {
                this.updateSkin(info);
            }
        }
    }

    private void clearSkinCache() {
        SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
        if (skinManager instanceof HDSkinManager) {
            ((HDSkinManager) skinManager).invalidateCache();
        }
    }

    @Override
    public void updateSelfSkin() {
        this.clearSkinCache();
        this.updateSkin(LabyMod.getInstance().getPlayerUUID());
    }

    private void updateSkin(UUID uniqueId) {
        if (Minecraft.getMinecraft().getConnection() == null) {
            return;
        }
        NetworkPlayerInfo info = Minecraft.getMinecraft().getConnection().getPlayerInfo(uniqueId);
        this.updateSkin(info);
    }

    private void updateSkin(NetworkPlayerInfo info) {
        if (TEXTURES_LOADED_FIELD == null || info == null) {
            return;
        }

        try {
            TEXTURES_LOADED_FIELD.setBoolean(info, false);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
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

    @Override
    public int getWindowHeight() {
        return LabyMod.getInstance().getDrawUtils().getScaledResolution().getScaledHeight();
    }

    @Override
    public int getWindowWidth() {
        return LabyMod.getInstance().getDrawUtils().getScaledResolution().getScaledWidth();
    }

    @Override
    public void renderPlayer(int x, int y, int mouseX, int mouseY, int size, int rotation) {
        if (LabyModCore.getMinecraft().getPlayer() == null || Minecraft.getMinecraft().getConnection() == null) {
            return;
        }

        DrawUtils.drawEntityOnScreen(x, y, size, mouseX, mouseY, rotation, 0, 0, LabyModCore.getMinecraft().getPlayer());
    }

    @Override
    public <T> T getJsonElement(String result, String key, Function<String, T> mapper) {
        JsonElement jsonElement = JsonParse.parse(result);
        return mapper.apply(jsonElement.getAsJsonObject().get(key).getAsString());
    }

}
