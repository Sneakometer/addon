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
package de.hdskins.labymod.shared.addon;

import de.hdskins.labymod.shared.config.AddonConfig;
import de.hdskins.labymod.shared.config.resolution.Resolution;
import de.hdskins.labymod.shared.event.MaxSkinResolutionChangeEvent;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.translation.TranslationRegistry;
import de.hdskins.labymod.shared.utils.Constants;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.client.NetworkClient;
import de.hdskins.protocol.concurrent.ListeningFuture;
import de.hdskins.protocol.packets.reading.client.PacketClientDeleteSkin;
import de.hdskins.protocol.packets.reading.client.PacketClientReportSkin;
import de.hdskins.protocol.packets.reading.client.PacketClientSetSlim;
import de.hdskins.protocol.packets.reading.client.PacketClientSkinSettings;
import de.hdskins.protocol.packets.reading.role.PacketClientGetRole;
import de.hdskins.protocol.packets.reading.role.PacketServerGetRoleResponse;
import de.hdskins.protocol.packets.reading.upload.PacketClientUploadSkin;
import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@ParametersAreNonnullByDefault
public class AddonContext {

    private static final Logger LOGGER = LogManager.getLogger(AddonContext.class);
    private static final ServerResult ERROR = new ServerResult(ExecutionStage.ERROR, null);
    private static final ServerResult NOT_CONNECTED = new ServerResult(ExecutionStage.NOT_CONNECTED, null);

    private final AddonConfig addonConfig;
    private final LabyModAddon labyModAddon;
    private final NetworkClient networkClient;
    private final TranslationRegistry translationRegistry;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    // changeable
    private UserRole userRole;

    public AddonContext(AddonConfig addonConfig, LabyModAddon labyModAddon, NetworkClient networkClient, TranslationRegistry translationRegistry) {
        this.addonConfig = addonConfig;
        this.labyModAddon = labyModAddon;
        this.networkClient = networkClient;
        this.translationRegistry = translationRegistry;
        this.loadUserRole();
    }

    public NetworkClient getNetworkClient() {
        return this.networkClient;
    }

    public LabyModAddon getLabyModAddon() {
        return this.labyModAddon;
    }

    public AddonConfig getAddonConfig() {
        return this.addonConfig;
    }

    public TranslationRegistry getTranslationRegistry() {
        return this.translationRegistry;
    }

    public AtomicBoolean getActive() {
        return this.active;
    }

    public AtomicBoolean getReconnecting() {
        return this.reconnecting;
    }

    @Nonnull
    public ServerResult uploadSkin(File skinFile) {
        if (!this.active.get() || this.reconnecting.get()) {
            return NOT_CONNECTED;
        }

        try {
            return new ServerResult(ExecutionStage.EXECUTING, this.networkClient.sendQuery(new PacketClientUploadSkin(Files.readAllBytes(skinFile.toPath()), this.addonConfig.isSlim())));
        } catch (IOException exception) {
            LOGGER.debug("Unable to upload skin from file {}", skinFile, exception);
            return ERROR;
        }
    }

    @Nonnull
    public ServerResult updateSlim(boolean slim) {
        if (!this.active.get() || this.reconnecting.get()) {
            return NOT_CONNECTED;
        }

        if (this.addonConfig.isSlim() == slim) {
            return ERROR;
        }

        this.addonConfig.setSlim(slim);
        return new ServerResult(ExecutionStage.EXECUTING, this.networkClient.sendQuery(new PacketClientSetSlim(slim)));
    }

    @Nonnull
    public ServerResult deleteSkin() {
        final UUID playerUniqueId = LabyMod.getInstance().getPlayerUUID();
        if (playerUniqueId == null) {
            return ERROR;
        }

        return this.deleteSkin(playerUniqueId);
    }

    @Nonnull
    public ServerResult deleteSkin(UUID playerUniqueId) {
        if (!this.active.get() || this.reconnecting.get()) {
            return NOT_CONNECTED;
        }

        return new ServerResult(ExecutionStage.EXECUTING, this.networkClient.sendQuery(new PacketClientDeleteSkin(playerUniqueId)));
    }

    @Nonnull
    public ServerResult reportSkin(UUID playerUniqueId) {
        if (!this.active.get() || this.reconnecting.get()) {
            return NOT_CONNECTED;
        }

        final UUID uniqueId = LabyMod.getInstance().getPlayerUUID();
        if (uniqueId == null || playerUniqueId.equals(uniqueId)) {
            return ERROR;
        }

        return new ServerResult(ExecutionStage.EXECUTING, this.networkClient.sendQuery(new PacketClientReportSkin(playerUniqueId)));
    }

    @Nonnull
    public UserRole getRole() {
        return this.userRole;
    }

    private void loadUserRole() {
        final UUID playerUniqueId = LabyMod.getInstance().getPlayerUUID();
        if (playerUniqueId == null || !this.active.get() || this.reconnecting.get()) {
            this.userRole = UserRole.USER;
        } else {
            PacketBase result = this.networkClient.sendQuery(new PacketClientGetRole(playerUniqueId)).getUninterruptedly();
            if (result instanceof PacketServerGetRoleResponse) {
                this.userRole = UserRole.roleFromOrdinalIndex(((PacketServerGetRoleResponse) result).getOrdinalIndex());
            } else {
                this.userRole = UserRole.USER;
            }
        }
    }

    public void updateRole(UserRole newRole) {
        this.userRole = newRole;
    }

    public void setMaxSkinResolution(Resolution resolution) {
        if (this.addonConfig.getMaxSkinResolution() != resolution) {
            this.networkClient.sendPacket(new PacketClientSkinSettings(resolution.getWidth(), resolution.getHeight()));
            this.addonConfig.setMaxSkinResolution(resolution);
            Constants.EVENT_BUS.postReported(MaxSkinResolutionChangeEvent.EVENT);
        }
    }

    public enum ExecutionStage {
        EXECUTING,
        NOT_CONNECTED,
        ERROR
    }

    public static final class ServerResult {
        private final ExecutionStage executionStage;
        private final ListeningFuture<PacketBase> future;

        public ServerResult(ExecutionStage executionStage, @Nullable ListeningFuture<PacketBase> future) {
            this.executionStage = executionStage;
            this.future = future;
        }

        public ExecutionStage getExecutionStage() {
            return this.executionStage;
        }

        public ListeningFuture<PacketBase> getFuture() {
            return this.future;
        }
    }
}
