package de.hdskins.labymod.shared.handler;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.gui.AdvancedBooleanElement;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;
import de.hdskins.labymod.shared.utils.ServerResult;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeleteSkinButtonClickHandler implements Runnable {

    public DeleteSkinButtonClickHandler(MinecraftAdapter minecraftAdapter, ConfigObject configObject, AdvancedBooleanElement slimElement) {
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
        this.slimElement = slimElement;
    }

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;
    private final AdvancedBooleanElement slimElement;

    private final AtomicBoolean deleteProcess = new AtomicBoolean();

    @Override
    public void run() {
        if (!this.deleteProcess.getAndSet(true)) {
            Constants.EXECUTOR.execute(() -> {
                ServerResult result = ServerHelper.deleteSkin(this.minecraftAdapter, this.configObject);
                if (result.getCode() == StatusCode.OK) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("delete-skin-success"));
                    this.minecraftAdapter.updateSelfSkin();
                } else if (result.getCode() == StatusCode.TOO_MANY_REQUESTS) {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("delete-skin-rate-limited"));
                } else {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("delete-skin-failed-unknown", result.getCode(), result.getMessage()));
                }

                this.slimElement.setCurrentValue(false);
                DeleteSkinButtonClickHandler.this.deleteProcess.set(false);
            });
        }
    }
}
