package de.hdskins.labymod.shared.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.labymod.api.LabyModAddon;

public class LabyModAddonConfig implements ConfigObject {

    private final LabyModAddon addon;

    private String serverUrl;
    private boolean showAllSkins;

    private LabyModAddonConfig(LabyModAddon addon) {
        this.addon = addon;
    }

    public static LabyModAddonConfig loadConfig(LabyModAddon addon) {
        LabyModAddonConfig labyModAddonConfig = new LabyModAddonConfig(addon);

        JsonElement serverUrl = addon.getConfig().get("server");
        if (serverUrl != null && !(serverUrl instanceof JsonNull)) {
            labyModAddonConfig.serverUrl = serverUrl.getAsString();
        } else {
            labyModAddonConfig.serverUrl = "http://api.hdskins.de";
        }

        labyModAddonConfig.showAllSkins = !addon.getConfig().has("allSkins") || addon.getConfig().get("allSkins").getAsBoolean();

        labyModAddonConfig.save();
        return labyModAddonConfig;
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
