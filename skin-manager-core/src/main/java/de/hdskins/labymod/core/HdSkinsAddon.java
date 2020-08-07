package de.hdskins.labymod.core;

import de.hdskins.labymod.core.config.MainConfig;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.mappings.HandledMappings;
import de.hdskins.labymod.shared.mappings.Mappings;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;
import net.labymod.api.LabyModAddon;
import net.labymod.main.Source;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class HdSkinsAddon extends LabyModAddon implements Consumer<String> {

    private MainConfig mainConfig;
    private HandledMappings mappings;
    private MinecraftAdapter minecraftAdapter;

    @Override
    public void onEnable() {
        Constants.aboutMcVersion = Source.ABOUT_MC_VERSION;

        this.mappings = HandledMappings.getLoadedMappings();
        if (this.mappings == null) {
            System.err.println(Constants.LOG_PREFIX + "Unable to load correct mappings for minecraft version " + Source.ABOUT_MC_VERSION);
            return;
        }

        System.out.println("\n        __  ______  _____ __   _\n" +
                "       / / / / __ \\/ ___// /__(_)___  _____\n" +
                "      / /_/ / / / /\\__ \\/ //_/ / __ \\/ ___/\n" +
                "     / __  / /_/ /___/ / ,< / / / / (__  )\n" +
                "    /_/ /_/_____//____/_/|_/_/_/ /_/____/\n" +
                "\n" +
                "          Copyright (c) 2020 HDSkins\n" +
                "   Support Discord: https://discord.gg/KN8rDZJ");

        Mappings mappings = this.mappings.getMappings();
        System.out.println(Constants.LOG_PREFIX + "Using mappings for " + mappings.getVersion() + " (Mappings version: " + mappings.getMappingsVersion() + ")");
    }

    @Override
    public void init(String addonName, UUID uuid) {
        if (this.mappings == null) {
            return;
        }

        super.init(addonName, uuid);
        switch (this.mappings) {
            case V1_8:
                this.minecraftAdapter = new de.hdskins.labymod.v18.V18MinecraftAdapter();
                de.hdskins.labymod.v18.manager.SkinManagerInjector.setNewSkinManager(this.mainConfig, this.mappings.getMappings());
                de.hdskins.labymod.v18.report.ReportUserActionEntryInvoker.addUserAction(this.minecraftAdapter, this.mainConfig);
                break;
            case V1_12:
                this.minecraftAdapter = new de.hdskins.labymod.v112.V112MinecraftAdapter();
                de.hdskins.labymod.v112.manager.SkinManagerInjector.setNewSkinManager(this.mainConfig, this.mappings.getMappings());
                de.hdskins.labymod.v112.report.ReportUserActionEntryInvoker.addUserAction(this.minecraftAdapter, this.mainConfig);
                break;
        }

        LanguageManager.setMinecraftAdapter(this.minecraftAdapter);
        this.minecraftAdapter.fillSettings(this.getSubSettings(), this.mainConfig, ServerHelper.isSlim(this.mainConfig));
        LanguageManager.registerLanguageUpdateListener(this);
    }

    @Override
    public void loadConfig() {
        this.mainConfig = MainConfig.loadConfig(this);
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
    }

    @Override
    public void onRenderPreview(int mouseX, int mouseY, float partialTicks) {
        LanguageManager.ensureLanguageSync();
    }

    @Override
    public void accept(String s) {
        if (this.minecraftAdapter != null) {
            this.minecraftAdapter.fillSettings(this.getSubSettings(), this.mainConfig, false);
        }
    }
}