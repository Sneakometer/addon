package de.hdskins.labymod.core;

import de.hdskins.labymod.core.config.MainConfig;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.mappings.HandledMappings;
import de.hdskins.labymod.shared.mappings.Mappings;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import net.labymod.api.LabyModAddon;
import net.labymod.main.Source;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;
import java.util.UUID;

public class HdSkinsAddon extends LabyModAddon {

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

        System.out.println("        __  ______  _____ __   _\n" +
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
                de.hdskins.labymod.v18.manager.SkinManagerInjector.setNewSkinManager(this.mainConfig, this.mappings.getMappings());
                this.minecraftAdapter = new de.hdskins.labymod.v18.V18MinecraftAdapter();
                break;
            case V1_12:
                de.hdskins.labymod.v112.manager.SkinManagerInjector.setNewSkinManager(this.mainConfig, this.mappings.getMappings());
                this.minecraftAdapter = new de.hdskins.labymod.v112.V112MinecraftAdapter();
                break;
        }

        this.minecraftAdapter.fillSettings(this.getSubSettings(), this.mainConfig);
    }

    @Override
    public void loadConfig() {
        this.mainConfig = MainConfig.loadConfig(this);
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
    }
}