package de.hdskins.labymod.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import de.hdskins.labymod.core.HdSkinsAddon;
import de.hdskins.labymod.shared.config.ConfigObject;

public class MainConfig implements ConfigObject {

    private final HdSkinsAddon addon;

    private String serverUrl;
    private String token;

    private MainConfig(HdSkinsAddon addon) {
        this.addon = addon;
    }

    public static MainConfig loadConfig(HdSkinsAddon addon) {
        MainConfig mainConfig = new MainConfig(addon);

        JsonElement serverUrl = addon.getConfig().get("server");
        if (serverUrl != null && !(serverUrl instanceof JsonNull)) {
            mainConfig.serverUrl = serverUrl.getAsString();
        }

        JsonElement adminToken = addon.getConfig().get("token");
        if (adminToken != null && !(adminToken instanceof JsonNull)) {
            mainConfig.token = adminToken.getAsString();
        }

        return mainConfig;
    }

    public void save() {
        this.addon.getConfig().addProperty("server", this.serverUrl);
        this.addon.getConfig().addProperty("token", this.token);

        this.addon.saveConfig();
    }

    @Override
    public String getServerUrl() {
        return serverUrl.endsWith("/") ? (this.serverUrl = this.serverUrl.substring(0, this.serverUrl.length() - 1)) : this.serverUrl;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setServerUrl(String url) {
        this.serverUrl = url;
        this.save();
    }

    @Override
    public void setToken(String token) {
        this.token = token;
        this.save();
    }
}
