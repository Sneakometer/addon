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
package de.hdskins.labymod.shared.config;

import de.hdskins.labymod.shared.config.resolution.Resolution;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collection;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface AddonConfig extends Serializable {

  @Nonnull
  String getServerHost();

  void setServerHost(String serverHost);

  int getServerPort();

  void setServerPort(int serverPort);

  @Nonnull
  String getGuidelinesUrl();

  @Nonnull
  InetAddress getServerAddress();

  long getFirstReconnectInterval();

  void setFirstReconnectInterval(long firstReconnectInterval);

  long getReconnectInterval();

  void setReconnectInterval(long reconnectInterval);

  long getQueryTimeoutMillis();

  void setQueryTimeoutMillis(long queryTimeoutMillis);

  long getSkinIdRequestTimeoutMillis();

  void setSkinIdRequestTimeoutMillis(long skinIdRequestTimeoutMillis);

  boolean showSkinsOfOtherPlayers();

  void setShowSkinsOfOtherPlayers(boolean showSkinsOfOtherPlayers);

  @Nonnull
  Resolution getMaxSkinResolution();

  void setMaxSkinResolution(Resolution resolution);

  boolean isSlim();

  void setSlim(boolean slim);

  @Nonnull
  Collection<UUID> getDisabledSkins();

  void removeAllDisabledSkins();

  void disableSkin(UUID playerUniqueId);

  void enableSkin(UUID playerUniqueId);

  boolean isSkinDisabled(UUID playerUniqueId);

  boolean hasAcceptedGuidelines();

  void setGuidelinesAccepted(boolean accepted);
}
