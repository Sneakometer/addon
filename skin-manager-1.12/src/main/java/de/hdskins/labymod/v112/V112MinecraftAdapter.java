package de.hdskins.labymod.v112;

import de.hdskins.labymod.shared.callbacks.SlimElementChangeConsumer;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.gui.ButtonElement;
import de.hdskins.labymod.shared.handler.DeleteSkinButtonClickHandler;
import de.hdskins.labymod.shared.handler.UploadFileButtonClickHandler;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.v112.gui.V112BooleanElement;
import de.hdskins.labymod.v112.gui.V112ButtonElement;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.Language;

import java.util.List;
import java.util.Optional;

public class V112MinecraftAdapter implements MinecraftAdapter {

    @Override
    public String getSessionId() {
        return Minecraft.getMinecraft().getSession().getToken();
    }

    @Override
    public void fillSettings(List<SettingsElement> list, ConfigObject object, boolean slim) {
        ButtonElement uploadSkinElement = new V112ButtonElement(
                LanguageManager.getTranslation("change-skin-option-name"),
                new ControlElement.IconData(Material.PAINTING),
                LanguageManager.getTranslation("button-click-here")
        );
        uploadSkinElement.setDescriptionText(LanguageManager.getTranslation("change-skin-option-description"));
        uploadSkinElement.setClickListener(new UploadFileButtonClickHandler(this, object));
        list.add(uploadSkinElement);

        ButtonElement deleteSkinElement = new V112ButtonElement(
                LanguageManager.getTranslation("delete-skin-option-name"),
                new ControlElement.IconData(Material.BARRIER),
                LanguageManager.getTranslation("button-click-here")
        );
        deleteSkinElement.setDescriptionText(LanguageManager.getTranslation("delete-skin-option-description"));
        deleteSkinElement.setClickListener(new DeleteSkinButtonClickHandler(this, object));
        list.add(deleteSkinElement);

        BooleanElement slimElement = new V112BooleanElement(
                LanguageManager.getTranslation("slim-skin-change-option"), new ControlElement.IconData(Material.REDSTONE_COMPARATOR),
                LanguageManager.getTranslation("slim-skin-option-on"), LanguageManager.getTranslation("slim-skin-option-off"),
                slim, new SlimElementChangeConsumer(object, this)
        );
        slimElement.setDescriptionText("Sets weather your skin is a slim (small) or default skin");
        list.add(slimElement);
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
        return language == null ? null : language.getLanguageCode();
    }
}
