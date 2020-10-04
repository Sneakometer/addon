package de.hdskins.labymod.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import de.hdskins.labymod.core.HdSkinsAddon;
import de.hdskins.labymod.shared.config.ConfigObject;

public class MainConfig implements ConfigObject {

    private final HdSkinsAddon addon;

    private String serverUrl;
    private boolean showAllSkins;

    private MainConfig(HdSkinsAddon addon) {
        this.addon = addon;
    }

    public static MainConfig loadConfig(HdSkinsAddon addon) {
        MainConfig mainConfig = new MainConfig(addon);

        JsonElement serverUrl = addon.getConfig().get("server");
        if (serverUrl != null && !(serverUrl instanceof JsonNull)) {
            mainConfig.serverUrl = serverUrl.getAsString();
        } else {
            mainConfig.serverUrl = "http://api.hdskins.de";
        }

        mainConfig.showAllSkins = !addon.getConfig().has("allSkins") || addon.getConfig().get("allSkins").getAsBoolean();

        mainConfig.save();
        return mainConfig;
    }

    public void save() {
        this.addon.getConfig().addProperty("server", this.serverUrl);
        this.addon.getConfig().addProperty("allSkins", this.showAllSkins);

        this.addon.saveConfig();
    }

    @Override
    public String getServerUrl() {
        return this.serverUrl == null ? null : serverUrl.endsWith("/") ? (this.serverUrl = this.serverUrl.substring(0, this.serverUrl.length() - 1)) : this.serverUrl;
    }

    @Override
    public void setShowAllSkins(boolean enabled) {
        if (this.showAllSkins == enabled) {
            return;
        }
        this.showAllSkins = enabled;
        this.save();
    }

    @Override
    public boolean shouldShowAllSkins() {
        return this.showAllSkins;
    }

}
