package de.hdskins.labymod.v112.useraction;

import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

public class ReloadUserActionEntry extends UserActionEntry {

    private final MinecraftAdapter minecraftAdapter;

    public ReloadUserActionEntry(MinecraftAdapter minecraftAdapter) {
        super(LanguageManager.getTranslation("refresh-skin-option-name"), EnumActionType.NONE, null, null);
        this.minecraftAdapter = minecraftAdapter;
    }

    @Override
    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
        this.minecraftAdapter.updateSkin(entityPlayer.getGameProfile().getId());
        this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("refresh-skin-cache-loading"));
    }
}
