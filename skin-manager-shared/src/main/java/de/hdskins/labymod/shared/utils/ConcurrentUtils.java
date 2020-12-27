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
package de.hdskins.labymod.shared.utils;

import de.hdskins.labymod.shared.concurrent.SilentCallable;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class ConcurrentUtils {

  private ConcurrentUtils() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  public static <T> T callOnClientThread(@Nonnull SilentCallable<T> callable) {
    if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
      return callable.call();
    } else {
      return ConcurrentUtils.getUninterruptedly(Minecraft.getMinecraft().addScheduledTask(callable));
    }
  }

  @Nullable
  public static <T> T getUninterruptedly(@Nonnull Future<T> future) {
    try {
      return future.get(15, TimeUnit.SECONDS);
    } catch (Throwable throwable) {
      return null;
    }
  }

  @Nonnull
  public static SilentCallable<Void> runnableToCallable(@Nonnull Runnable runnable) {
    return () -> {
      runnable.run();
      return null;
    };
  }
}
