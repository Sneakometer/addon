/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.hdskins.labymod.shared.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.hdskins.labymod.shared.config.resolution.Resolution;
import de.hdskins.labymod.shared.event.ConfigChangeEvent;
import de.hdskins.labymod.shared.utils.Constants;
import net.labymod.addon.AddonLoader;
import net.labymod.api.LabyModAddon;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public class JsonAddonConfig implements AddonConfig {
    // networking magic
    private static final int FIRST_NON_ROOT_PORT = 1025;
    private static final int MAX_POSSIBLE_PORT_NUMBER = 65535;
    // time magic
    private static final long MIN_RECONNECT_TIME = TimeUnit.SECONDS.toMillis(1);
    // json utils
    private static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Type CONFIG_TYPE = new TypeToken<JsonAddonConfig>() {
    }.getType();
    // lazy initialized by load(LabyModAddon) method
    private static Path configPath;
    // visibility settings
    private final Collection<UUID> disabledSkins;
    // server settings
    private String serverHost;
    private int serverPort;
    // connection settings
    private long firstReconnectInterval;
    private long reconnectInterval;
    // client settings
    private boolean slim;
    private Resolution maxSkinResolution;
    private boolean showSkinsOfOtherPlayers;

    private JsonAddonConfig() {
        this.serverHost = "api.hdskins.de";
        this.serverPort = 7007;
        this.firstReconnectInterval = TimeUnit.SECONDS.toMillis(10);
        this.reconnectInterval = TimeUnit.SECONDS.toMillis(5);
        this.slim = false;
        this.disabledSkins = new ArrayList<>();
        this.maxSkinResolution = Resolution.RESOLUTION_ALL;
        this.showSkinsOfOtherPlayers = true;
    }

    public static AddonConfig load(LabyModAddon labyModAddon) {
        if (configPath == null) {
            configPath = AddonLoader.getConfigDirectory().toPath().resolve(labyModAddon.about.name + ".json");
        }

        final JsonObject config = labyModAddon.getConfig();
        if (!config.has("config") || config.get("config").isJsonNull()) {
            config.add("config", GSON.toJsonTree(new JsonAddonConfig()));
        }

        return GSON.fromJson(config.get("config").getAsJsonObject(), CONFIG_TYPE);
    }

    @Nonnull
    @Override
    public String getServerHost() {
        return this.serverHost;
    }

    @Override
    public void setServerHost(String serverHost) {
        if (!this.serverHost.equals(serverHost)) {
            this.serverHost = serverHost;
            this.save();
        }
    }

    @Override
    public int getServerPort() {
        return Math.min(Math.max(this.serverPort, FIRST_NON_ROOT_PORT), MAX_POSSIBLE_PORT_NUMBER);
    }

    @Override
    public void setServerPort(int serverPort) {
        if (this.serverPort != serverPort && serverPort >= FIRST_NON_ROOT_PORT && serverPort <= MAX_POSSIBLE_PORT_NUMBER) {
            this.serverPort = serverPort;
            this.save();
        }
    }

    @Override
    public long getFirstReconnectInterval() {
        return Math.max(this.firstReconnectInterval, MIN_RECONNECT_TIME);
    }

    @Override
    public void setFirstReconnectInterval(long firstReconnectInterval) {
        if (this.firstReconnectInterval != firstReconnectInterval && firstReconnectInterval >= MIN_RECONNECT_TIME) {
            this.firstReconnectInterval = firstReconnectInterval;
            this.save();
        }
    }

    @Override
    public long getReconnectInterval() {
        return Math.min(1000, this.reconnectInterval);
    }

    @Override
    public void setReconnectInterval(long reconnectInterval) {
        if (this.reconnectInterval != reconnectInterval && reconnectInterval >= MIN_RECONNECT_TIME) {
            this.reconnectInterval = reconnectInterval;
            this.save();
        }
    }

    @Override
    public boolean showSkinsOfOtherPlayers() {
        return this.showSkinsOfOtherPlayers;
    }

    @Override
    public void setShowSkinsOfOtherPlayers(boolean showSkinsOfOtherPlayers) {
        if (this.showSkinsOfOtherPlayers != showSkinsOfOtherPlayers) {
            this.showSkinsOfOtherPlayers = showSkinsOfOtherPlayers;
            this.save();
        }
    }

    @Nonnull
    @Override
    public Resolution getMaxSkinResolution() {
        return this.maxSkinResolution;
    }

    @Override
    public void setMaxSkinResolution(Resolution resolution) {
        if (this.maxSkinResolution != resolution) {
            this.maxSkinResolution = resolution;
            this.save();
        }
    }

    @Override
    public boolean isSlim() {
        return this.slim;
    }

    @Override
    public void setSlim(boolean slim) {
        if (this.slim != slim) {
            this.slim = slim;
            this.save();
        }
    }

    @Nonnull
    @Override
    public Collection<UUID> getDisabledSkins() {
        return this.disabledSkins;
    }

    @Override
    public void removeAllDisabledSkins() {
        if (!this.disabledSkins.isEmpty()) {
            this.disabledSkins.clear();
            this.save();
        }
    }

    @Override
    public void disableSkin(UUID playerUniqueId) {
        if (!this.disabledSkins.contains(playerUniqueId)) {
            this.disabledSkins.add(playerUniqueId);
            this.save();
        }
    }

    @Override
    public void enableSkin(UUID playerUniqueId) {
        if (this.disabledSkins.contains(playerUniqueId)) {
            this.disabledSkins.remove(playerUniqueId);
            this.save();
        }
    }

    @Override
    public boolean isSkinDisabled(UUID playerUniqueId) {
        return this.disabledSkins.contains(playerUniqueId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        return o.getClass().equals(this.getClass()) && EqualsBuilder.reflectionEquals(this, o, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    private void save() {
        try (Writer out = new OutputStreamWriter(Files.newOutputStream(configPath), StandardCharsets.UTF_8)) {
            JsonObject object = new JsonObject();
            object.add("config", GSON.toJsonTree(this));
            GSON.toJson(object, out);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        Constants.EVENT_BUS.postReported(new ConfigChangeEvent(this));
    }
}
