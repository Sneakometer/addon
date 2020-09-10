package de.hdskins.labymod.shared.callbacks;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class SlimElementChangeConsumer implements Function<Boolean, CompletableFuture<Boolean>> {

    public SlimElementChangeConsumer(ConfigObject configObject, MinecraftAdapter minecraftAdapter) {
        this.configObject = configObject;
        this.minecraftAdapter = minecraftAdapter;
    }

    private final ConfigObject configObject;
    private final MinecraftAdapter minecraftAdapter;
    private final AtomicBoolean updateProcessRunning = new AtomicBoolean();

    @Override
    public CompletableFuture<Boolean> apply(Boolean aBoolean) {
        if (!this.updateProcessRunning.getAndSet(true)) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            Constants.EXECUTOR.execute(() -> {
                StatusCode statusCode = ServerHelper.setSlim(this.minecraftAdapter, this.configObject, aBoolean);
                String s = aBoolean ? "slim" : "default";
                if (statusCode == StatusCode.OK) {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-successfully-toggled", s));
                    this.minecraftAdapter.invalidateSkinCache();
                    future.complete(aBoolean);
                } else if (statusCode == StatusCode.TOO_MANY_REQUESTS) {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-rate-limited"));
                    future.complete(!aBoolean);
                } else if (statusCode == StatusCode.NO_CONTENT) {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-no-hd-skin"));
                    future.complete(!aBoolean);
                } else {
                    this.minecraftAdapter.changeToIngame();
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-toggle-failed-unknown", s, statusCode));
                    future.complete(!aBoolean);
                }

                SlimElementChangeConsumer.this.updateProcessRunning.set(false);
            });

            return future;
        }

        return null;
    }
}
