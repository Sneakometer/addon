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
package de.hdskins.labymod.shared.config.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.hdskins.labymod.shared.config.JsonAddonConfig;

import java.lang.reflect.Type;

public class ServerConfigSerializer implements JsonSerializer<JsonAddonConfig.ServerConfig>, JsonDeserializer<JsonAddonConfig.ServerConfig> {

    private final JsonAddonConfig.ServerConfig defaultServerConfig;

    public ServerConfigSerializer(JsonAddonConfig.ServerConfig defaultServerConfig) {
        this.defaultServerConfig = defaultServerConfig;
    }

    @Override
    public JsonAddonConfig.ServerConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            JsonElement host = object.get("host");
            JsonElement port = object.get("port");

            if ((host == null || host.isJsonNull()) && (port == null || port.isJsonNull())) {
                return this.defaultServerConfig;
            } else {
                return new JsonAddonConfig.ServerConfig(
                    host == null || host.isJsonNull() ? this.defaultServerConfig.getHost() : host.getAsString(),
                    port == null || port.isJsonNull() ? this.defaultServerConfig.getPort() : port.getAsInt()
                );
            }
        } else {
            throw new JsonParseException("JsonElement " + json + " is not a json object");
        }
    }

    @Override
    public JsonElement serialize(JsonAddonConfig.ServerConfig src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.getHost() != null && src.getHost().equals(this.defaultServerConfig.getHost())
            && src.getPort() != null && src.getPort().equals(this.defaultServerConfig.getPort())) {
            return new JsonObject();
        } else {
            JsonObject object = new JsonObject();
            object.addProperty("host", src.getHost());
            object.addProperty("port", src.getPort());
            return object;
        }
    }
}
