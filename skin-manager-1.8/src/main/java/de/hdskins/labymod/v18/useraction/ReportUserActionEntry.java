package de.hdskins.labymod.v18.useraction;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.test.Constants;
import de.hdskins.labymod.test.config.ConfigObject;
import de.hdskins.labymod.test.language.LanguageManager;
import de.hdskins.labymod.test.minecraft.MinecraftAdapter;
import de.hdskins.labymod.test.profile.PlayerProfile;
import de.hdskins.labymod.test.utils.ServerHelper;
import de.hdskins.labymod.test.utils.ServerResult;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

public class ReportUserActionEntry extends UserActionEntry {

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;

    public ReportUserActionEntry(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        super(LanguageManager.getTranslation("user-skin-report-choose-display-name"), EnumActionType.NONE, null, null);
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    @Override
    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
        Constants.EXECUTOR.execute(() -> {
            PlayerProfile playerProfile = new PlayerProfile(entityPlayer.getName(), entityPlayer.getGameProfile().getId());
            ServerResult result = ServerHelper.reportSkin(playerProfile, this.minecraftAdapter, this.configObject);
            if (result.getCode() == StatusCode.OK) {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-success", playerProfile.getName()));
            } else if (result.getCode() == StatusCode.TOO_MANY_REQUESTS) {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-rate-limited"));
            } else if (result.getCode() == StatusCode.I_AM_A_TEAPOT) {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-no-hd-skin"));
            } else {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-failed-unknown", result.getCode(), result.getMessage()));
            }
        });
    }
}
