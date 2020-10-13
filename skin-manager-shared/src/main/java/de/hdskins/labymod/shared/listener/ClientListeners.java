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
import de.hdskins.labymod.shared.event.TranslationLanguageCodeChangeEvent;
import de.hdskins.labymod.shared.settings.SettingInvoker;
import de.hdskins.labymod.shared.settings.SettingsFactory;
import de.hdskins.labymod.shared.texture.HDSkinManager;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public final class ClientListeners {

    private final HDSkinManager hdSkinManager;
    private UUID currentUniqueId;

    public ClientListeners(HDSkinManager hdSkinManager) {
        this.hdSkinManager = hdSkinManager;
        this.currentUniqueId = LabyMod.getInstance().getPlayerUUID();
    }

    @SubscribeEvent
    public void handle(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        UUID currentlyUsedUniqueId = LabyMod.getInstance().getPlayerUUID();
        if (currentlyUsedUniqueId != null && (this.currentUniqueId == null || !this.currentUniqueId.equals(currentlyUsedUniqueId))) {
            this.currentUniqueId = currentlyUsedUniqueId;
            // We need to reconnect to the server as the client changed his unique id
            if (!this.hdSkinManager.getAddonContext().getReconnecting().getAndSet(true)) {
                // Disable the skin manager for now
                this.hdSkinManager.getAddonContext().getActive().set(false);
                // We prevent now close the connection to the server
                this.hdSkinManager.getAddonContext().getNetworkClient().getChannel().close();
                // And now we can reconnect to the server
                BackendUtils.reconnect(this.hdSkinManager.getAddonContext()).thenRunAsync(() -> {
                    // We are now connected to the server again so we can re-enable the skin manager
                    this.hdSkinManager.getAddonContext().getActive().set(true);
                    this.hdSkinManager.getAddonContext().getReconnecting().set(false);
                });
            }
        }
    }

    @SubscribeEvent
    public void handle(TranslationLanguageCodeChangeEvent event) {
        SettingInvoker.unloadSettingElements();
        for (SettingsElement element : SettingsFactory.bakeSettings(this.hdSkinManager.getAddonContext())) {
            SettingInvoker.addSettingsElement(element);
        }
    }
}
