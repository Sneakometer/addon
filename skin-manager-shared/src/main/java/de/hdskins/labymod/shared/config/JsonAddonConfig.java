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
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import de.hdskins.labymod.shared.config.gson.ServerConfigSerializer;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public class JsonAddonConfig implements AddonConfig {
    // server default network stuff
    // these values aren't in the config as they may change between releases and
    // are only useful for development reasons (normally)
    private static final int DEFAULT_PORT = 2007;
    private static final String DEFAULT_HOST = "api.hdskins.de";
    private static final ServerConfig DEFAULT_SERVER_CONFIG = new ServerConfig(DEFAULT_HOST, DEFAULT_PORT);
    private static final InetAddress DEFAULT_SERVER_ADDRESS = new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT).getAddress();
    // networking magic
    private static final int FIRST_NON_ROOT_PORT = 1025;
    private static final int MAX_POSSIBLE_PORT_NUMBER = 65535;
    // time magic
    private static final long MIN_RECONNECT_TIME = TimeUnit.SECONDS.toMillis(1);
    // json utils
    private static final JsonParser PARSER = new JsonParser();
    private static final Gson GSON = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapter(ServerConfig.class, new ServerConfigSerializer(DEFAULT_SERVER_CONFIG))
        .create();
    private static final Type CONFIG_TYPE = new TypeToken<JsonAddonConfig>() {
    }.getType();
    // lazy initialized by load (LabyModAddon) method
    private static Path configPath;
    // visibility settings
    private final Collection<UUID> disabledSkins;
    // server settings
    private final ServerConfig serverConfig;
    // lazy initialized by getServerAddress() call
    private transient InetAddress serverAddress;
    // connection settings
    private long firstReconnectInterval;
    private long reconnectInterval;
    // client settings
    private boolean slim;
    private Resolution maxSkinResolution;
    private boolean showSkinsOfOtherPlayers;

    private JsonAddonConfig() {
        this.serverConfig = DEFAULT_SERVER_CONFIG;
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

        if (Files.notExists(configPath)) {
            JsonAddonConfig addonConfig = new JsonAddonConfig();
            addonConfig.save();
            return addonConfig;
        }

        try (InputStream inputStream = Files.newInputStream(configPath);
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JsonObject config = PARSER.parse(reader).getAsJsonObject();

            if (!config.has("configuration") || config.get("configuration").isJsonNull()) {
                JsonAddonConfig addonConfig = new JsonAddonConfig();
                addonConfig.save();
                return addonConfig;
            }

            return GSON.fromJson(config.get("configuration").getAsJsonObject(), CONFIG_TYPE);
        } catch (IOException exception) {
            exception.printStackTrace();
            return new JsonAddonConfig();
        }
    }

    @Nonnull
    @Override
    public String getServerHost() {
        return this.serverConfig.host == null ? DEFAULT_HOST : this.serverConfig.host;
    }

    @Override
    public void setServerHost(String serverHost) {
        if (this.serverConfig.host == null || !this.serverConfig.host.equals(serverHost)) {
            this.serverConfig.host = serverHost;
            this.serverAddress = null;
            this.save();
        }
    }

    @Override
    public int getServerPort() {
        return Math.min(Math.max(this.serverConfig.port == null ? DEFAULT_PORT : this.serverConfig.port, FIRST_NON_ROOT_PORT), MAX_POSSIBLE_PORT_NUMBER);
    }

    @Override
    public void setServerPort(int serverPort) {
        if (this.serverConfig.port == null || this.serverConfig.port != serverPort && serverPort >= FIRST_NON_ROOT_PORT && serverPort <= MAX_POSSIBLE_PORT_NUMBER) {
            this.serverConfig.port = serverPort;
            this.save();
        }
    }

    @Nonnull
    @Override
    public InetAddress getServerAddress() {
        try {
            return this.serverAddress == null
                ? this.serverAddress = InetAddress.getByName(this.getServerHost())
                : this.serverAddress;
        } catch (UnknownHostException exception) {
            return DEFAULT_SERVER_ADDRESS;
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
        final Path parent = configPath.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException exception) {
                exception.printStackTrace();
                return;
            }
        }

        try (Writer out = new OutputStreamWriter(Files.newOutputStream(configPath), StandardCharsets.UTF_8)) {
            JsonObject object = new JsonObject();
            object.add("configuration", GSON.toJsonTree(this));
            GSON.toJson(object, out);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        Constants.EVENT_BUS.postReported(new ConfigChangeEvent(this));
    }

    public static class ServerConfig {
        private String host;
        private Integer port;

        public ServerConfig(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return this.host;
        }

        public Integer getPort() {
            return this.port;
        }
    }
}
