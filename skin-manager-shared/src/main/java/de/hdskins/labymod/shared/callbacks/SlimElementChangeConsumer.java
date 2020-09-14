package de.hdskins.labymod.shared.callbacks;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;
import de.hdskins.labymod.shared.utils.ServerResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class SlimElementChangeConsumer implements Function<Boolean, CompletableFuture<Boolean>> {

    public SlimElementChangeConsumer(ConfigObject configObject, MinecraftAdapter minecraftAdapter) {
        this.configObject = configObject;
        this.minecraftAdapter = minecraftAdapter;
    }

    private final ConfigObject configObject;
    private final MinecraftAdapter minecraftAdapter;
    private final AtomicLong lastPress = new AtomicLong(-1);

    @Override
    public CompletableFuture<Boolean> apply(Boolean aBoolean) {
        long last = this.lastPress.get();
        if (last != -1 && last > System.currentTimeMillis() + 5000) {
            return null;
        }
        this.lastPress.set(System.currentTimeMillis());

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Constants.EXECUTOR.execute(() -> {
            ServerResult result = ServerHelper.setSlim(this.minecraftAdapter, this.configObject, aBoolean);
            String s = aBoolean ? "slim" : "default";
            if (result.getCode() == StatusCode.OK) {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-successfully-toggled", s));
                this.minecraftAdapter.invalidateSkinCache();
                future.complete(aBoolean);
            } else if (result.getCode() == StatusCode.TOO_MANY_REQUESTS) {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-rate-limited"));
                future.complete(!aBoolean);
            } else if (result.getCode() == StatusCode.NO_CONTENT) {
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-no-hd-skin"));
                future.complete(!aBoolean);
            } else {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("slim-toggle-failed-unknown", s, result.getCode(), result.getMessage()));
                future.complete(!aBoolean);
            }
        });

        return future;
    }
}
