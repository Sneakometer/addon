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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.gui.GuiFriendsLayout;
import net.labymod.labyconnect.packets.PacketPlayRequestAddFriend;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class UnbanRequestUtil {

  private static final Set<String> EA_REQUEST_NAMES = ImmutableSet.of("HDSkinsDE");

  private UnbanRequestUtil() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public static CompletableFuture<RequestResult> tryOpenRequest(@Nonnull AddonContext context) {
    final LabyConnect labyConnect = LabyMod.getInstance().getLabyConnect();
    if (!labyConnect.isOnline()) {
      return CompletableFuture.completedFuture(RequestResult.failure(context.getTranslationRegistry().translateMessage("laby-connect-not-connected")));
    }
    final ChatUser hdSkins = getFriendByDefaultNames(labyConnect);
    if (hdSkins != null) {
      return CompletableFuture.completedFuture(redirectToChat(context, hdSkins));
    }
    final String target = Iterables.getFirst(EA_REQUEST_NAMES, null);
    if (target == null) {
      throw new RuntimeException("No player selected for ua request target");
    }
    return sendRequest(labyConnect, target).thenApplyAsync(v -> {
      final ChatUser user = awaitAdd(labyConnect, target, TimeUnit.SECONDS.toMillis(10));
      if (user != null) {
        return ConcurrentUtils.callOnClientThread(() -> redirectToChat(context, user));
      }
      return RequestResult.failure(context.getTranslationRegistry().translateMessage("laby-connect-friend-request-timed-out"));
    }, Constants.EXECUTOR);
  }

  @Nullable
  private static ChatUser getFriendByDefaultNames(@Nonnull LabyConnect connect) {
    for (ChatUser friend : connect.getFriends()) {
      if (friend.getGameProfile().getName() != null && EA_REQUEST_NAMES.contains(friend.getGameProfile().getName())) {
        return friend;
      }
    }
    return null;
  }

  @Nullable
  private static ChatUser getFriendByName(@Nonnull LabyConnect connect, @Nonnull String name) {
    for (ChatUser friend : connect.getFriends()) {
      if (friend.getGameProfile().getName() != null && friend.getGameProfile().getName().equals(name)) {
        return friend;
      }
    }
    return null;
  }

  @Nullable
  private static ChatUser awaitAdd(@Nonnull LabyConnect connect, @Nonnull String name, long timeoutMillis) {
    final long timeout = System.currentTimeMillis() + timeoutMillis;

    ChatUser result = null;
    while (timeout >= System.currentTimeMillis() && (result = getFriendByName(connect, name)) == null) {
      sleep();
    }
    return result;
  }

  @Nonnull
  private static CompletableFuture<Void> sendRequest(@Nonnull LabyConnect connect, @Nonnull String name) {
    return CompletableFuture.supplyAsync(() -> {
      connect.getClientConnection().sendPacket(new PacketPlayRequestAddFriend(name));
      return null;
    });
  }

  @Nonnull
  public static RequestResult redirectToChat(@Nonnull AddonContext context, @Nonnull ChatUser chatUser) {
    try {
      final GuiFriendsLayout layout = new GuiFriendsLayout();
      layout.initLayout();
      layout.getChatElementPartnerProfile().setPartner(chatUser);
      Minecraft.getMinecraft().displayGuiScreen(layout);
      return RequestResult.success(context.getTranslationRegistry().translateMessage("laby-connect-successfully-redirected"));
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      return RequestResult.failure(context.getTranslationRegistry().translateMessage("laby-connect-internal-error"));
    }
  }

  private static void sleep() {
    try {
      Thread.sleep(500);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt(); // Not much we can do here
    }
  }

  public static class RequestResult {

    private final boolean success;
    private final String message;

    private RequestResult(boolean success, String message) {
      this.success = success;
      this.message = message;
    }

    @Nonnull
    public static RequestResult failure(@Nonnull String message) {
      return new RequestResult(false, message);
    }

    @Nonnull
    public static RequestResult success(@Nonnull String message) {
      return new RequestResult(true, message);
    }

    public boolean isSuccess() {
      return this.success;
    }

    public String getMessage() {
      return this.message;
    }
  }
}
