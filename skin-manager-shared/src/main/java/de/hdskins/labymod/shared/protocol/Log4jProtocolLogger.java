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
package de.hdskins.labymod.shared.protocol;

import de.hdskins.protocol.logger.InternalLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Log4jProtocolLogger implements InternalLogger {

  public static final Log4jProtocolLogger INSTANCE = new Log4jProtocolLogger();
  private static final Logger LOGGER = LogManager.getLogger("Protocol");

  private Log4jProtocolLogger() {
  }

  @Override
  public void handleException(Throwable throwable) {
    LOGGER.error(throwable);
  }

  @Override
  public void debug(String s) {
    LOGGER.debug(s);
  }

  @Override
  public void info(String s) {
    LOGGER.info(s);
  }

  @Override
  public void warn(String s) {
    LOGGER.warn(s);
  }

  @Override
  public void error(String s, @Nullable Throwable throwable) {
    LOGGER.error(s, throwable);
  }
}
