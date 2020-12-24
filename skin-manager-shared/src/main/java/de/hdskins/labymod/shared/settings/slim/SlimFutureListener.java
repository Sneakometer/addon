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
package de.hdskins.labymod.shared.settings.slim;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.notify.NotificationUtil;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.concurrent.FutureListener;
import de.hdskins.protocol.packets.reading.client.PacketServerQueryResponse;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class SlimFutureListener implements FutureListener<PacketBase>, Constants {

  private final boolean target;
  private final AddonContext addonContext;
  private final CompletableFuture<Boolean> future;

  public SlimFutureListener(boolean target, AddonContext addonContext, CompletableFuture<Boolean> future) {
    this.target = target;
    this.addonContext = addonContext;
    this.future = future;
  }

  @Override
  public void nullResult() {
    NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("slim-toggle-failed-unknown"));
    this.addonContext.getAddonConfig().setSlim(!this.target);
    this.future.complete(!this.target);
  }

  @Override
  public void nonNullResult(PacketBase packetBase) {
    if (packetBase instanceof PacketServerQueryResponse) {
      PacketServerQueryResponse response = (PacketServerQueryResponse) packetBase;
      if (response.isSuccess()) {
        String targetResult = this.target ? "slim" : "default";
        NotificationUtil.notify(SUCCESS, this.addonContext.getTranslationRegistry().translateMessage("slim-successfully-toggled", new Object[]{targetResult}));
        this.future.complete(this.target);
      } else {
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage(response.getReason(), response.getReason()));
        this.addonContext.getAddonConfig().setSlim(!this.target);
        this.future.complete(!this.target);
      }
    } else {
      NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("slim-toggle-failed-unknown"));
      this.addonContext.getAddonConfig().setSlim(!this.target);
      this.future.complete(!this.target);
    }
  }

  @Override
  public void cancelled() {
    NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("slim-toggle-failed-unknown"));
    this.addonContext.getAddonConfig().setSlim(!this.target);
    this.future.complete(!this.target);
  }
}
