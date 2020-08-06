package de.hdskins.labymod.v112;

import de.hdskins.labymod.shared.callbacks.SlimElementChangeConsumer;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.gui.ButtonElement;
import de.hdskins.labymod.shared.handler.DeleteSkinButtonClickHandler;
import de.hdskins.labymod.shared.handler.UploadFileButtonClickHandler;
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

import java.util.List;
import java.util.Optional;

public class V112MinecraftAdapter implements MinecraftAdapter {

    @Override
    public String getSessionId() {
        return Minecraft.getMinecraft().getSession().getToken();
    }

    @Override
    public void fillSettings(List<SettingsElement> list, ConfigObject object, boolean slim) {
        ButtonElement uploadSkinElement = new V112ButtonElement("Change skin", new ControlElement.IconData(Material.PAINTING), "Click here");
        uploadSkinElement.setDescriptionText("Skins must not contain right-wing, narcissistic, offensive or sexual content.");
        uploadSkinElement.setClickListener(new UploadFileButtonClickHandler(this, object));
        list.add(uploadSkinElement);

        ButtonElement deleteSkinElement = new V112ButtonElement("Delete skin", new ControlElement.IconData(Material.BARRIER), "Click here");
        deleteSkinElement.setDescriptionText("Deletes your skin permanently from the skin server. If you just want to change your skin, user the change skin option. " +
                "After this operation uploading a new skin is blocked for 2 minutes");
        deleteSkinElement.setClickListener(new DeleteSkinButtonClickHandler(this, object));
        list.add(deleteSkinElement);

        BooleanElement slimElement = new V112BooleanElement(
                "Slim skin", new ControlElement.IconData(Material.REDSTONE_COMPARATOR),
                "Slim", "Default",
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
}
