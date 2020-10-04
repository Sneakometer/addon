package de.hdskins.labymod.shared.callbacks;

import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ShowSkinElementChangeConsumer implements Function<Boolean, CompletableFuture<Boolean>> {

    private final MinecraftAdapter minecraftAdapter;
    private long nextExecution = -1;

    public ShowSkinElementChangeConsumer(MinecraftAdapter minecraftAdapter) {
        this.minecraftAdapter = minecraftAdapter;
    }

    @Override
    public CompletableFuture<Boolean> apply(Boolean aBoolean) {
        if (this.nextExecution != -1 && this.nextExecution > System.currentTimeMillis()) {
            this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("show-all-skins-rate-limit"));
            this.minecraftAdapter.changeToIngame();
            return null;
        }
        this.nextExecution = System.currentTimeMillis() + 30000;

        ConfigObject config = this.minecraftAdapter.getConfig();
        config.setShowAllSkins(!config.shouldShowAllSkins());

        this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation(config.shouldShowAllSkins() ? "show-all-skins-enabled" : "show-all-skins-disabled"));
        this.minecraftAdapter.changeToIngame();
        this.minecraftAdapter.invalidateSkinCache();

        return null;
    }
}
