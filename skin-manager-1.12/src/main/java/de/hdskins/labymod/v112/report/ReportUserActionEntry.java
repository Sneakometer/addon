package de.hdskins.labymod.v112.report;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import de.hdskins.labymod.shared.utils.ServerHelper;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReportUserActionEntry extends UserActionEntry {

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;
    private final AtomicBoolean reportActionRunning = new AtomicBoolean();

    public ReportUserActionEntry(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        super(LanguageManager.getTranslation("user-skin-report-choose-display-name"), EnumActionType.NONE, null, null);
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    @Override
    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
        if (!reportActionRunning.getAndSet(true)) {
            Constants.EXECUTOR.execute(() -> {
                PlayerProfile playerProfile = new PlayerProfile(entityPlayer.getName(), entityPlayer.getGameProfile().getId());
                StatusCode statusCode = ServerHelper.reportSkin(playerProfile, this.minecraftAdapter, this.configObject);
                if (statusCode == StatusCode.OK) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-success", playerProfile.getName()));
                } else if (statusCode == StatusCode.TOO_MANY_REQUESTS) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-rate-limited"));
                } else if (statusCode == StatusCode.I_AM_A_TEAPOT) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-no-hd-skin"));
                } else {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("user-skin-report-failed-unknown", statusCode));
                }

                ReportUserActionEntry.this.reportActionRunning.set(false);
            });
        }
    }
}
