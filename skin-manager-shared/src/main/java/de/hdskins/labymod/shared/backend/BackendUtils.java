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
package de.hdskins.labymod.shared.backend;

import com.mojang.authlib.exceptions.AuthenticationException;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.config.AddonConfig;
import de.hdskins.protocol.client.NetworkClient;
import de.hdskins.protocol.packets.reading.client.PacketClientSkinSettings;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BackendUtils {

    private static final Logger LOGGER = LogManager.getLogger(BackendUtils.class);
    private static final Supplier<String> NAME_SUPPLIER = LabyMod.getInstance()::getPlayerName;
    private static final Function<String, Boolean> SERVER_JOINER = serverId -> {
        final Session session = Minecraft.getMinecraft().getSession();
        if (session != null) {
            try {
                Minecraft.getMinecraft().getSessionService().joinServer(session.getProfile(), session.getToken(), serverId);
                return true;
            } catch (AuthenticationException exception) {
                exception.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    };

    private BackendUtils() {
        throw new UnsupportedOperationException();
    }

    public static CompletableFuture<NetworkClient> connectToServer(AddonConfig addonConfig) {
        return CompletableFuture.supplyAsync(() -> {
            NetworkClient networkClient = NetworkClient.create(addonConfig.getServerAddress().getHostAddress(), addonConfig.getServerPort(), NAME_SUPPLIER, SERVER_JOINER);
            networkClient.setFirstReconnectInterval(addonConfig.getFirstReconnectInterval());
            networkClient.setReconnectInterval(addonConfig.getReconnectInterval());
            return connect0(networkClient, addonConfig);
        }).thenApply(networkClient -> {
            networkClient.sendPacket(new PacketClientSkinSettings(
                addonConfig.getMaxSkinResolution().getWidth(),
                addonConfig.getMaxSkinResolution().getHeight()
            ));
            return networkClient;
        });
    }

    public static CompletableFuture<Void> reconnect(AddonContext addonContext) {
        return CompletableFuture.supplyAsync(() -> {
            connect0(addonContext.getNetworkClient(), addonContext.getAddonConfig()).sendPacket(new PacketClientSkinSettings(
                addonContext.getAddonConfig().getMaxSkinResolution().getWidth(),
                addonContext.getAddonConfig().getMaxSkinResolution().getHeight()
            ));
            return null;
        });
    }

    private static NetworkClient connect0(NetworkClient networkClient, AddonConfig addonConfig) {
        if (networkClient.connect()) {
            LOGGER.debug("Successfully connected to network server {}:{} after the first attempt", addonConfig.getServerHost(), addonConfig.getServerPort());
            return networkClient;
        }

        int reconnectAttempts = 0;
        do {
            if (reconnectAttempts++ == 0) {
                sleep(networkClient.getFirstReconnectInterval());
            } else {
                sleep(networkClient.getReconnectInterval());

                LOGGER.debug(
                    "Connection attempt to server at {}:{} failed the {} time. (reconnect times: first: {}, always: {}) Retry...",
                    addonConfig.getServerHost(), addonConfig.getServerPort(), reconnectAttempts,
                    networkClient.getFirstReconnectInterval(), networkClient.getReconnectInterval()
                );
            }
        } while (!networkClient.connect());

        LOGGER.debug("Successfully connected to network server {}:{} after the {} attempt", addonConfig.getServerHost(), addonConfig.getServerPort(), reconnectAttempts);
        return networkClient;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
