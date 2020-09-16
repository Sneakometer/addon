package de.hdskins.labymod.v18.report;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;
import de.hdskins.labymod.shared.utils.ServerResult;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

public class DeleteUserSkinActionEntry extends UserActionEntry {

    private final MinecraftAdapter minecraftAdapter;

    public DeleteUserSkinActionEntry(MinecraftAdapter minecraftAdapter) {
        super(LanguageManager.getTranslation("team-delete-skin-button"), EnumActionType.NONE, null, null);
        this.minecraftAdapter = minecraftAdapter;
    }

    @Override
    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
        ServerResult result = ServerHelper.forceDeleteSkin(this.minecraftAdapter, this.minecraftAdapter.getConfig(), entityPlayer.getUniqueID());
        if (result.getCode() == StatusCode.OK) {
            this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("team-delete-skin-successfully", entityPlayer.getName()));
        } else if (result.getCode() == StatusCode.I_AM_A_TEAPOT) {
            this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-no-hd-skin"));
        } else {
            this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("team-delete-skin-error", result.getCode(), result.getMessage()));
        }
    }
}