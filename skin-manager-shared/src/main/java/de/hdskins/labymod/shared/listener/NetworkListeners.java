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
package de.hdskins.labymod.shared.listener;

import de.hdskins.labymod.shared.backend.BackendUtils;
import de.hdskins.labymod.shared.texture.HDSkinManager;
import de.hdskins.protocol.listener.ChannelInactiveListener;
import de.hdskins.protocol.listener.PacketListener;
import de.hdskins.protocol.packets.reading.live.*;
import io.netty.channel.Channel;
import net.labymod.main.LabyMod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NetworkListeners {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final HDSkinManager hdSkinManager;

    public NetworkListeners(HDSkinManager hdSkinManager) {
        this.hdSkinManager = hdSkinManager;
    }

    @ChannelInactiveListener
    @SuppressWarnings("unused")
    public void handleChannelInactive(Channel channel) {
        if (this.hdSkinManager.getAddonContext().getActive().getAndSet(false) && !this.hdSkinManager.getAddonContext().getReconnecting().getAndSet(true)) {
            // The skin manager is still active and not reconnecting so lets do it!
            BackendUtils.reconnect(this.hdSkinManager.getAddonContext()).thenRunAsync(() -> {
                // We are now connected to the server again so we can re-enable the skin manager
                this.hdSkinManager.getAddonContext().getActive().set(true);
                this.hdSkinManager.getAddonContext().getReconnecting().set(false);
            });
        }
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
    public void handleDisplayMessage(PacketServerDisplayChatMessage packet) {
        final String message;
        if (packet.isTranslationKey()) {
            message = this.hdSkinManager.getAddonContext().getTranslationRegistry().translateMessage(packet.getMessage(), EMPTY_OBJECT_ARRAY);
        } else {
            message = packet.getMessage();
        }

        LabyMod.getInstance().displayMessageInChat(message);
    }
}
