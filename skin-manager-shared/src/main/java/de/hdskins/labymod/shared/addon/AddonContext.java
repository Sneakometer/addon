/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 - 2021 HD-Skins <https://github.com/HDSkins>
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

import com.google.common.base.Preconditions;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.config.AddonConfig;
import de.hdskins.labymod.shared.config.resolution.Resolution;
import de.hdskins.labymod.shared.event.MaxSkinResolutionChangeEvent;
import de.hdskins.labymod.shared.event.UserBanUpdateEvent;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.translation.TranslationRegistry;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.client.NetworkClient;
import de.hdskins.protocol.component.ClientSettings;
import de.hdskins.protocol.concurrent.ListeningFuture;
import de.hdskins.protocol.packets.reading.client.PacketClientDeleteSkin;
import de.hdskins.protocol.packets.reading.client.PacketClientReportSkin;
import de.hdskins.protocol.packets.reading.client.PacketClientSetSlim;
import de.hdskins.protocol.packets.reading.client.PacketClientSkinSettings;
import de.hdskins.protocol.packets.reading.client.PacketClientUploadSkin;
import de.hdskins.protocol.packets.reading.connection.PacketClientPing;
import de.hdskins.protocol.packets.reading.connection.PacketServerPong;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateBan;
import de.hdskins.protocol.packets.reading.ratelimit.PacketServerUpdateRateLimits;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@ParametersAreNonnullByDefault
public class AddonContext {

  private static final Logger LOGGER = LogManager.getLogger(AddonContext.class);
  private static final ServerResult ERROR = new ServerResult(ExecutionStage.ERROR, null);
  private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(1);
  private static final ServerResult NOT_CONNECTED = new ServerResult(ExecutionStage.NOT_CONNECTED, null);
  private static final PacketServerUpdateRateLimits.RateLimits EMPTY = PacketServerUpdateRateLimits.RateLimits.limits((short) -1, (short) -1, (short) -1, (short) -1);

  private final AddonConfig addonConfig;
  private final LabyModAddon labyModAddon;
  private final NetworkClient networkClient;
  private final TranslationRegistry translationRegistry;
  private final PingHelper pingHelper = new PingHelper();
  private final AtomicBoolean active = new AtomicBoolean(true);
  private final AtomicBoolean reconnecting = new AtomicBoolean(false);
  // changeable
  private BanInfo currentBan;
  private HDSkinManager skinManager;
  private ClientSettings clientSettings;
  private UserRole userRole = UserRole.USER;
  private PacketServerUpdateRateLimits.RateLimits rateLimits = EMPTY;

  public AddonContext(AddonConfig addonConfig, LabyModAddon labyModAddon, NetworkClient networkClient, TranslationRegistry translationRegistry) {
    this.addonConfig = addonConfig;
    this.labyModAddon = labyModAddon;
    this.networkClient = networkClient;
    this.translationRegistry = translationRegistry;
    this.clientSettings = new ClientSettings(addonConfig.getMaxSkinResolution().getWidth(), addonConfig.getMaxSkinResolution().getHeight());
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

  public HDSkinManager getSkinManager() {
    return this.skinManager;
  }

  public void setSkinManager(HDSkinManager skinManager) {
    Preconditions.checkArgument(this.skinManager == null, "Cannot redefine singleton skin manager");
    this.skinManager = skinManager;
  }

  public PingHelper getPingHelper() {
    return this.pingHelper;
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

  public void updateRole(UserRole newRole) {
    this.userRole = newRole;
  }

  public void setMaxSkinResolution(Resolution resolution) {
    if (this.addonConfig.getMaxSkinResolution() != resolution) {
      final Resolution before = this.addonConfig.getMaxSkinResolution();
      this.addonConfig.setMaxSkinResolution(resolution);
      this.clientSettings = new ClientSettings(resolution.getWidth(), resolution.getHeight());
      this.networkClient.sendPacket(new PacketClientSkinSettings(this.clientSettings));
      Constants.EVENT_BUS.postReported(new MaxSkinResolutionChangeEvent(resolution, before));
    }
  }

  @Nullable
  public BanInfo getCurrentBan() {
    return this.currentBan;
  }

  public void setCurrentBan(@Nullable PacketServerLiveUpdateBan currentBan) {
    this.currentBan = currentBan == null ? null : new BanInfo(currentBan);
    Constants.EVENT_BUS.postReported(new UserBanUpdateEvent(currentBan != null));
  }

  public PacketServerUpdateRateLimits.RateLimits getRateLimits() {
    return this.rateLimits;
  }

  public void setRateLimits(PacketServerUpdateRateLimits.RateLimits rateLimits) {
    this.rateLimits = rateLimits;
  }

  public void reconnect() {
    if (!this.reconnecting.getAndSet(true)) {
      // Disable the skin manager for now
      this.active.set(false);
      // We prevent now close the connection to the server
      this.networkClient.getChannel().close();
      // And now we can reconnect to the server
      BackendUtils.reconnect(this).thenRunAsync(() -> {
        // We are now connected to the server again so we can re-enable the skin manager
        this.active.set(true);
        this.reconnecting.set(false);
        this.skinManager.initConnection();
      });
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

  public static final class BanInfo {

    private final long timoutMillis;
    private final PacketServerLiveUpdateBan info;

    public BanInfo(PacketServerLiveUpdateBan ban) {
      this.info = ban;
      this.timoutMillis = System.currentTimeMillis() + ban.getTimeout();
    }

    public long getTimoutMillis() {
      return this.timoutMillis;
    }

    public PacketServerLiveUpdateBan getInfo() {
      return this.info;
    }
  }

  private static final class PingWrapper {

    private long sentTime;
    private PacketClientPing ping;

    public long getSentTime() {
      return this.sentTime;
    }

    @Nullable
    public PacketClientPing getPing() {
      return this.ping;
    }

    public void setPing(PacketClientPing ping) {
      this.ping = ping;
      this.sentTime = System.currentTimeMillis();
    }
  }

  public final class PingHelper {

    private final AtomicLong lastPing = new AtomicLong();
    private final PingWrapper pingWrapper = new PingWrapper();

    public PingHelper() {
      SERVICE.scheduleAtFixedRate(() -> {
        if (AddonContext.this.active.get() && !AddonContext.this.reconnecting.get()) {
          byte transactionId = 0;
          if (this.pingWrapper.getPing() != null) {
            transactionId = this.pingWrapper.getPing().getTransactionId();
            if (transactionId >= Byte.MAX_VALUE) {
              transactionId = 0;
            }
          }

          final PacketClientPing ping = new PacketClientPing(transactionId);
          this.pingWrapper.setPing(ping);
          AddonContext.this.networkClient.sendPacket(ping);
        }
      }, 0, 1, TimeUnit.MINUTES);
    }

    public void handlePong(@Nonnull PacketServerPong pong) {
      if (this.pingWrapper.getPing() != null && this.pingWrapper.getPing().getTransactionId() == pong.getTransactionId()) {
        this.lastPing.set(System.currentTimeMillis() - this.pingWrapper.getSentTime());
      }
    }

    public long getLastPing() {
      return this.lastPing.get();
    }
  }
}
