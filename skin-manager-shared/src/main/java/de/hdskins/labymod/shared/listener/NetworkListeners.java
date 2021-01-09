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
package de.hdskins.labymod.shared.listener;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.actions.ActionFactory;
import de.hdskins.labymod.shared.actions.ActionInvoker;
import de.hdskins.labymod.shared.actions.MarkedUserActionEntry;
import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.event.TranslationLanguageCodeChangeEvent;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.settings.SettingInvoker;
import de.hdskins.labymod.shared.translation.TranslationRegistry;
import de.hdskins.labymod.shared.utils.LabyModUtils;
import de.hdskins.protocol.listener.ChannelInactiveListener;
import de.hdskins.protocol.listener.PacketListener;
import de.hdskins.protocol.packets.general.PacketDisconnect;
import de.hdskins.protocol.packets.reading.live.PacketServerDisplayChatMessage;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateBan;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateDeletePlayer;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateDeleteSkin;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateRole;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateSkin;
import de.hdskins.protocol.packets.reading.live.PacketServerLiveUpdateSlim;
import de.hdskins.protocol.packets.reading.other.PacketServerOverrideMessage;
import de.hdskins.protocol.packets.reading.ratelimit.PacketServerUpdateRateLimits;
import io.netty.channel.Channel;
import net.labymod.main.LabyMod;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public final class NetworkListeners {

  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  private final HDSkinManager hdSkinManager;
  private final TranslationRegistry translationRegistry;

  public NetworkListeners(HDSkinManager hdSkinManager, TranslationRegistry translationRegistry) {
    this.hdSkinManager = hdSkinManager;
    this.translationRegistry = translationRegistry;
  }

  @ChannelInactiveListener
  @SuppressWarnings("unused")
  public void handleChannelInactive(Channel channel) {
    if (this.hdSkinManager.getAddonContext().getActive().getAndSet(false) && !this.hdSkinManager.getAddonContext().getReconnecting().getAndSet(true)) {
      // Reset the translation registry to remove the language overrides
      this.hdSkinManager.getAddonContext().getTranslationRegistry().reset();
      // The skin manager is still active and not reconnecting so lets do it!
      BackendUtils.reconnect(this.hdSkinManager.getAddonContext()).thenRunAsync(() -> {
        // We are now connected to the server again so we can re-enable the skin manager
        this.hdSkinManager.getAddonContext().getActive().set(true);
        this.hdSkinManager.getAddonContext().getReconnecting().set(false);
      });
    }
  }

  @PacketListener
  public void handleLanguageOverride(PacketServerOverrideMessage packet) {
    packet.getMessages().forEach((key, message) ->
      this.translationRegistry.updateTranslation(packet.getLanguage(), key, message));

    // update the elements
    Constants.EVENT_BUS.postReported(TranslationLanguageCodeChangeEvent.EVENT);
  }

  @PacketListener
  public void handleLiveSkinUpdate(PacketServerLiveUpdateSkin packet) {
    this.hdSkinManager.pushSkinUpdate(packet.getUniqueId(), packet.getSkinId());
  }

  @PacketListener
  public void handleLiveSkinSlimUpdate(PacketServerLiveUpdateSlim packet) {
    this.hdSkinManager.pushSkinSlimChange(packet.getUniqueId(), packet.isSlim());
  }

  @PacketListener
  public void handleLiveDeleteByHash(PacketServerLiveUpdateDeleteSkin packet) {
    this.hdSkinManager.pushSkinDelete(packet.getSkinId());
  }

  @PacketListener
  public void handleLiveDeleteByPlayer(PacketServerLiveUpdateDeletePlayer packet) {
    this.hdSkinManager.pushSkinDelete(packet.getUniqueId());
  }

  @PacketListener
  public void handleUserRoleUpdate(PacketServerLiveUpdateRole packet) {
    final UserRole newRole = UserRole.roleFromOrdinalIndex(packet.getOrdinalIndex());
    final boolean staffBefore = this.hdSkinManager.getAddonContext().getRole().isHigherOrEqualThan(UserRole.STAFF);
    final boolean staffNow = newRole.isHigherOrEqualThan(UserRole.STAFF);

    this.hdSkinManager.getAddonContext().updateRole(newRole);
    if (staffNow != staffBefore) {
      ActionInvoker.unregisterMarkedEntries();
      for (MarkedUserActionEntry entry : ActionFactory.bakeUserActionEntries(this.hdSkinManager.getAddonContext())) {
        ActionInvoker.addUserActionEntry(entry);
      }
    }
  }

  @PacketListener
  public void handleDisplayMessage(PacketServerDisplayChatMessage packet) {
    final String message;
    if (packet.isTranslationKey()) {
      message = this.hdSkinManager.getAddonContext().getTranslationRegistry().translateMessage(packet.getMessage(), EMPTY_OBJECT_ARRAY);
    } else {
      message = packet.getMessage();
    }

    LabyMod.getInstance().displayMessageInChat(message);
  }

  @PacketListener
  public void handleDisconnect(PacketDisconnect packet) {
    this.handleDisplayMessage(packet);
  }

  @PacketListener
  public void handleBanUpdate(PacketServerLiveUpdateBan packet) {
    SettingInvoker.pushSettingStateUpdate(!packet.isBanned());
    if (packet.isBanned()) {
      ActionInvoker.unregisterMarkedEntries();
    } else {
      ActionFactory.bakeUserActionEntries(this.hdSkinManager.getAddonContext());
    }

    String message = packet.getReason();
    if (packet.isTranslate()) {
      message = this.hdSkinManager.getAddonContext().getTranslationRegistry().translateMessage(packet.getReason(), EMPTY_OBJECT_ARRAY);
    }

    LabyModUtils.displayAchievement((packet.isBanned() ? Constants.FAILURE : Constants.SUCCESS) + " BAN UPDATE", message);
  }

  @PacketListener
  public void handleRateLimitUpdate(PacketServerUpdateRateLimits packet) {
    this.hdSkinManager.getAddonContext().setRateLimits(packet.getRateLimits());
  }
}
