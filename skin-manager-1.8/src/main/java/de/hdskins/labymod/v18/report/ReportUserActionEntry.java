package de.hdskins.labymod.v18.report;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.shared.utils.ServerHelper;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

public class ReportUserActionEntry extends UserActionEntry {

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;

    public ReportUserActionEntry(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        super("Report HD Skin", EnumActionType.NONE, null, null);
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    @Override
    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
        Constants.EXECUTOR.execute(() -> {
            PlayerProfile playerProfile = new PlayerProfile(entityPlayer.getName(), entityPlayer.getGameProfile().getId());
            StatusCode statusCode = ServerHelper.reportSkin(playerProfile, this.minecraftAdapter, this.configObject);
            if (statusCode == StatusCode.OK) {
                this.minecraftAdapter.displayMessageInChat("§aSuccessfully §7reported skin of user " + playerProfile.getName() + ". Thanks for you report");
            } else if (statusCode == StatusCode.TOO_MANY_REQUESTS) {
                this.minecraftAdapter.displayMessageInChat("§cYou can only report a skin every two minutes");
            } else if (statusCode == StatusCode.I_AM_A_TEAPOT) {
                this.minecraftAdapter.displayMessageInChat("§cFailed to report skin of user! This user has no HD skin!");
            } else {
                this.minecraftAdapter.displayMessageInChat("§cFailed to report skin of user! Server answer status code was: " + statusCode);
            }
        });
    }
}
