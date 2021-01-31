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

import de.hdskins.labymod.shared.actions.ActionFactory;
import de.hdskins.labymod.shared.actions.ActionInvoker;
import de.hdskins.labymod.shared.actions.MarkedUserActionEntry;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.event.MaxSkinResolutionChangeEvent;
import de.hdskins.labymod.shared.event.TranslationLanguageCodeChangeEvent;
import de.hdskins.labymod.shared.eventbus.EventListener;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import de.hdskins.labymod.shared.settings.SettingInvoker;
import de.hdskins.labymod.shared.settings.SettingsFactory;
import io.netty.channel.Channel;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
public final class ClientListeners {

  private final HDSkinManager hdSkinManager;
  private final AtomicInteger currentTick = new AtomicInteger();
  private UUID currentUniqueId;

  public ClientListeners(HDSkinManager hdSkinManager) {
    this.hdSkinManager = hdSkinManager;
    this.currentUniqueId = LabyMod.getInstance().getPlayerUUID();
  }

  @SubscribeEvent
  public void handle(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      // Evaluate if we did a full tick (every second)
      final boolean fullTick = this.currentTick.incrementAndGet() >= 20;
      if (fullTick) {
        this.currentTick.set(0);
      }
      // Sync client language with internal translation registry language code
      // every second is enough for the language
      final AddonContext addonContext = this.hdSkinManager.getAddonContext();
      if (fullTick) {
        addonContext.getTranslationRegistry().reSyncLanguageCode();
      }
      // Check if the player changed his
      final UUID currentlyUsedUniqueId = LabyMod.getInstance().getPlayerUUID();
      if (currentlyUsedUniqueId != null && (this.currentUniqueId == null || !this.currentUniqueId.equals(currentlyUsedUniqueId))) {
        this.currentUniqueId = currentlyUsedUniqueId;
        // We need to reconnect to the server as the client changed his unique id
        addonContext.reconnect();
      }
      final Channel channel = this.hdSkinManager.getAddonContext().getNetworkClient().getChannel();
      if (!channel.isActive() && !channel.isWritable() && !channel.isOpen()) {
        // The server connection was lost
        addonContext.reconnect();
      }
    }
  }

  @EventListener
  public void handle(TranslationLanguageCodeChangeEvent event) {
    SettingInvoker.unloadSettingElements();
    for (SettingsElement element : SettingsFactory.bakeSettings(this.hdSkinManager.getAddonContext())) {
      SettingInvoker.addSettingsElement(element);
    }

    ActionInvoker.unregisterMarkedEntries();
    for (MarkedUserActionEntry entry : ActionFactory.bakeUserActionEntries(this.hdSkinManager.getAddonContext())) {
      ActionInvoker.addUserActionEntry(entry);
    }
  }

  @EventListener
  public void handle(MaxSkinResolutionChangeEvent event) {
    this.hdSkinManager.pushMaxResolutionUpdate(event.getNow(), event.getBefore());
  }
}
