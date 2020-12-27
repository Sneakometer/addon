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
package de.hdskins.labymod.shared.asm.draw;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import de.hdskins.labymod.shared.concurrent.ConcurrentUtil;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import net.labymod.main.LabyMod;
import net.labymod.utils.UUIDFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public final class SkullRenderer {

  private static final String DEFAULT_NAME = "Steve";
  private static final String EMPTY_PLACEHOLDER_PROPERTY_NAME = "__place__holder__";
  private static final ModelSkeletonHead MODEL_SKELETON_HEAD = new ModelHumanoidHead();
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  private static final Property PLACEHOLDER_PROPERTY = new Property("", EMPTY_PLACEHOLDER_PROPERTY_NAME);
  private static final Cache<MinecraftProfileTexture, ResourceLocation> CACHE = CacheBuilder.newBuilder()
    .expireAfterAccess(30, TimeUnit.SECONDS)
    .ticker(Ticker.systemTicker())
    .concurrencyLevel(4)
    .build();
  private static final Cache<GameProfile, GameProfile> GAME_PROFILE_CACHE = CacheBuilder.newBuilder()
    .expireAfterAccess(30, TimeUnit.SECONDS)
    .ticker(Ticker.systemTicker())
    .concurrencyLevel(4)
    .build();

  private SkullRenderer() {
    throw new UnsupportedOperationException();
  }

  public static void renderSkull(GameProfile gameProfile) {
    ResourceLocation skullResourceLocation = loadTextureResourceLocation(gameProfile);
    Minecraft.getMinecraft().getTextureManager().bindTexture(skullResourceLocation);

    GlStateManager.pushMatrix();
    GlStateManager.disableCull();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableAlpha();
    GlStateManager.scale(-1.0F, 1.0F, 1.0F);
    GlStateManager.translate(0.0F, 0.2F, 0.0F);
    MODEL_SKELETON_HEAD.render(null, 0.0F, 0.0F, 0.0F, 180.0F, 0.0F, 0.0625F);
    GlStateManager.popMatrix();
  }

  public static void drawPlayerHead(GameProfile gameProfile, int x, int y, int size) {
    if (gameProfile != null && gameProfile.getId() == null) {
      drawPlayerHead(gameProfile.getName(), x, y, size);
    } else {
      ResourceLocation skullResourceLocation = loadTextureResourceLocation(gameProfile);
      LabyMod.getInstance().getDrawUtils().drawPlayerHead(skullResourceLocation, x, y, size);
    }
  }

  public static void drawPlayerHead(UUID uniqueId, int x, int y, int size) {
    drawPlayerHead(new GameProfile(uniqueId, DEFAULT_NAME), x, y, size);
  }

  public static void drawPlayerHead(String username, int x, int y, int size) {
    if (username != null) {
      UUIDFetcher.getUUID(username, uniqueId -> {
        ConcurrentUtil.call(() -> {
          if (uniqueId != null) {
            drawPlayerHead(new GameProfile(uniqueId, username), x, y, size);
          } else {
            LabyMod.getInstance().getDrawUtils().drawPlayerHead(DefaultPlayerSkin.getDefaultSkinLegacy(), x, y, size);
          }
          return null;
        });
      });
    } else {
      LabyMod.getInstance().getDrawUtils().drawPlayerHead(DefaultPlayerSkin.getDefaultSkinLegacy(), x, y, size);
    }
  }

  private static ResourceLocation loadTextureResourceLocation(GameProfile profile) {
    // hack:
    // LabyMod calls this method for rendering the skulls in the main menu as well as in the chat using
    // session.getProfile(). This method always generates a new GameProfile so we have to cache and
    // re-use the old one, as this profile gets filled with the profile textures of the user
    GameProfile gameProfile = GAME_PROFILE_CACHE.getIfPresent(profile);
    if (gameProfile == null) {
      GAME_PROFILE_CACHE.put(profile, gameProfile = profile);
    }
    return loadTextureResourceLocation0(gameProfile);
  }

  private static ResourceLocation loadTextureResourceLocation0(GameProfile profile) {
    ResourceLocation resourceLocation = getSkin(profile);
    if (profile != null) {
      final SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
      Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = skinManager.loadSkinFromCache(profile);
      if (!(skinManager instanceof HDSkinManager) && (map.isEmpty() || !map.containsKey(MinecraftProfileTexture.Type.SKIN))) {
        // hack:
        // the default skin manager of minecraft caches the skins (profile - profile) but
        // if the textures were set lazily the textures in the map are still empty so
        // we have to get them now if they are set to not use the stupid cache from
        // minecraft.
        // also this check if not needed if we use our internal skin manager as we keep
        // track of these changes and maintain them in our backing cache
        map = Minecraft.getMinecraft().getSessionService().getTextures(profile, false);
      }

      if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
        final ResourceLocation cachedLocation = CACHE.getIfPresent(map.get(MinecraftProfileTexture.Type.SKIN));
        if (cachedLocation != null) {
          return cachedLocation;
        } else {
          resourceLocation = skinManager.loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, (type, location, texture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
              CACHE.put(texture, location);
            }
          });
        }
      } else if (profile.getProperties().isEmpty() || (!profile.getProperties().containsKey("textures") && !profile.getProperties().containsKey(EMPTY_PLACEHOLDER_PROPERTY_NAME))) {
        profile.getProperties().put(EMPTY_PLACEHOLDER_PROPERTY_NAME, PLACEHOLDER_PROPERTY); // Set to prevent duplicate lookups
        EXECUTOR_SERVICE.execute(() -> {
          Minecraft.getMinecraft().getSessionService().fillProfileProperties(profile, false);
          profile.getProperties().removeAll(EMPTY_PLACEHOLDER_PROPERTY_NAME);
        });
      } else {
        UUID uuid = EntityPlayer.getUUID(profile);
        resourceLocation = DefaultPlayerSkin.getDefaultSkin(uuid);
      }
    }

    return resourceLocation;
  }

  @Nonnull
  private static ResourceLocation getSkin(@Nullable GameProfile profile) {
    return profile == null || profile.getId() == null
      ? DefaultPlayerSkin.getDefaultSkinLegacy()
      : DefaultPlayerSkin.getDefaultSkin(profile.getId());
  }
}
