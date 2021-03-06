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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.gui.GuiFriendsLayout;
import net.labymod.labyconnect.log.ChatlogManager;
import net.labymod.labyconnect.log.SingleChat;
import net.labymod.labyconnect.packets.PacketPlayFriendRemove;
import net.labymod.labyconnect.packets.PacketPlayRequestAddFriend;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import org.apache.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class UnbanRequestUtils {

  private static final String USER_LOOKUP_URL = "http://api.hdskins.de/labymod/chat/users";
  private static final Set<LabyChatUser> EA_REQUEST_BOTS = loadUnbanRequestBotProfiles();

  private UnbanRequestUtils() {
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
    final LabyChatUser target = EA_REQUEST_BOTS.isEmpty() ? null : Iterables.get(EA_REQUEST_BOTS, ThreadLocalRandom.current().nextInt(EA_REQUEST_BOTS.size()), null);
    if (target == null) {
      return CompletableFuture.completedFuture(RequestResult.failure(context.getTranslationRegistry().translateMessage("laby-connect-no-bots-available")));
    }
    return sendRequest(labyConnect, target.getName()).thenApplyAsync(v -> {
      final ChatUser user = awaitAdd(labyConnect, target.getUniqueId(), TimeUnit.SECONDS.toMillis(10));
      if (user != null) {
        return ConcurrentUtils.callOnClientThread(() -> redirectToChat(context, user));
      }
      return RequestResult.failure(context.getTranslationRegistry().translateMessage("laby-connect-friend-request-timed-out"));
    }, Constants.EXECUTOR);
  }

  @Nullable
  private static ChatUser getFriendByDefaultNames(@Nonnull LabyConnect connect) {
    for (ChatUser friend : connect.getFriends()) {
      if (friend.getGameProfile().getId() != null && isOurBot(friend.getGameProfile().getId())) {
        return friend;
      }
    }
    return null;
  }

  @Nullable
  private static ChatUser awaitAdd(@Nonnull LabyConnect connect, @Nonnull UUID uniqueId, long timeoutMillis) {
    final long timeout = System.currentTimeMillis() + timeoutMillis;

    ChatUser result = null;
    while (timeout >= System.currentTimeMillis() && (result = connect.getChatUserByUUID(uniqueId)) == null) {
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

  public static void handleFriendRemove(@Nonnull PacketPlayFriendRemove packet) {
    final ChatlogManager chatlogManager = LabyMod.getInstance().getLabyConnect().getChatlogManager();
    if (isOurBot(packet.getToRemove().getGameProfile().getName())) {
      final SingleChat chat = chatlogManager.getChat(packet.getToRemove());
      chat.getMessages().clear();
      chatlogManager.saveChatlogs(LabyMod.getInstance().getPlayerUUID());
    }
  }

  @Nonnull
  private static Set<LabyChatUser> loadUnbanRequestBotProfiles() {
    return HttpUtils.doGet(USER_LOOKUP_URL, HttpStatus.SC_OK, httpResponse -> {
      final ImmutableSet.Builder<LabyChatUser> out = ImmutableSet.builder();
      try (InputStreamReader reader = new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8)) {
        final JsonArray array = Constants.JSON_PARSER.parse(reader).getAsJsonArray();
        for (JsonElement jsonElement : array) {
          out.add(Constants.GSON.fromJson(jsonElement, LabyChatUser.class));
        }
      }
      return out.build();
    }, ImmutableSet.of());
  }

  private static boolean isOurBot(@Nonnull String name) {
    for (LabyChatUser user : EA_REQUEST_BOTS) {
      if (user.getName().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isOurBot(@Nonnull UUID uniqueId) {
    for (LabyChatUser user : EA_REQUEST_BOTS) {
      if (user.getUniqueId().equals(uniqueId)) {
        return true;
      }
    }
    return false;
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

  public static final class LabyChatUser {

    private final String name;
    private final UUID uniqueId;

    public LabyChatUser(String name, UUID uniqueId) {
      this.name = name;
      this.uniqueId = uniqueId;
    }

    public String getName() {
      return this.name;
    }

    public UUID getUniqueId() {
      return this.uniqueId;
    }
  }
}
