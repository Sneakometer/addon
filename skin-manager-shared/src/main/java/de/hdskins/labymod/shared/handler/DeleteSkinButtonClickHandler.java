package de.hdskins.labymod.shared.handler;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeleteSkinButtonClickHandler implements Runnable {

    public DeleteSkinButtonClickHandler(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;

    private final AtomicBoolean deleteProcess = new AtomicBoolean();

    @Override
    public void run() {
        if (!this.deleteProcess.getAndSet(true)) {
            Constants.EXECUTOR.execute(() -> {
                StatusCode statusCode = ServerHelper.deleteSkin(this.minecraftAdapter, this.configObject);
                if (statusCode == StatusCode.OK) {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("delete-skin-success"));
                    this.minecraftAdapter.invalidateSkinCache();
                } else if (statusCode == StatusCode.TOO_MANY_REQUESTS) {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("delete-skin-rate-limited"));
                } else {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("delete-skin-failed-unknown", statusCode));
                }

                DeleteSkinButtonClickHandler.this.deleteProcess.set(false);
            });
        }
    }
}
