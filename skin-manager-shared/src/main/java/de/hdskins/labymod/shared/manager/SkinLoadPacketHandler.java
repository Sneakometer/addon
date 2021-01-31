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
package de.hdskins.labymod.shared.manager;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.config.resolution.Resolution;
import de.hdskins.labymod.shared.resource.HDResourceLocation;
import de.hdskins.labymod.shared.texture.HDMinecraftProfileTexture;
import de.hdskins.labymod.shared.texture.HDSkinTexture;
import de.hdskins.labymod.shared.utils.ConcurrentUtils;
import de.hdskins.protocol.PacketBase;
import de.hdskins.protocol.packets.reading.download.PacketServerResponseSkin;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class SkinLoadPacketHandler implements Consumer<PacketBase> {

  private static final Logger LOGGER = LogManager.getLogger(SkinLoadPacketHandler.class);

  private final Path targetLocalPath;
  private final AddonContext addonContext;
  private final HDSkinManager skinManager;
  private final HDResourceLocation location;
  private final TextureManager textureManager;
  private final Runnable backingLoaderExecutor;
  private final MinecraftProfileTexture texture;
  private final MinecraftProfileTexture.Type type;
  private final SkinManager.SkinAvailableCallback callback;

  protected SkinLoadPacketHandler(Path targetLocalPath, HDSkinManager manager, HDResourceLocation location, TextureManager textureManager, AddonContext context,
                                  Runnable backingLoaderExecutor, MinecraftProfileTexture texture, MinecraftProfileTexture.Type type,
                                  SkinManager.SkinAvailableCallback callback) {
    this.targetLocalPath = targetLocalPath;
    this.addonContext = context;
    this.skinManager = manager;
    this.location = location;
    this.textureManager = textureManager;
    this.backingLoaderExecutor = backingLoaderExecutor;
    this.texture = texture;
    this.type = type;
    this.callback = callback;
  }

  @Override
  public void accept(PacketBase packetBase) {
    if (packetBase instanceof PacketServerResponseSkin) {
      PacketServerResponseSkin response = (PacketServerResponseSkin) packetBase;
      if (response.getSkinData().length == 0) {
        LOGGER.debug("Unable to load skin with hash {} because server sent an empty result", this.texture.getHash());
        ConcurrentUtils.callOnClientThread(ConcurrentUtils.runnableToCallable(this.backingLoaderExecutor));
        return;
      }

      final Path parent = this.targetLocalPath.getParent();
      if (parent != null && Files.notExists(parent)) {
        try {
          Files.createDirectories(parent);
        } catch (IOException exception) {
          LOGGER.debug("Unable to create directory {} for skin download to {}", parent, this.targetLocalPath, exception);
          ConcurrentUtils.callOnClientThread(ConcurrentUtils.runnableToCallable(this.backingLoaderExecutor));
          return;
        }
      }

      try (InputStream stream = new ByteArrayInputStream(response.getSkinData())) {
        final BufferedImage image = SkinLoadImageProcessor.process(stream);
        try (OutputStream outputStream = Files.newOutputStream(this.targetLocalPath, StandardOpenOption.CREATE)) {
          ImageIO.write(image, "png", outputStream);
        }
        // Update the skin resolution to the texture location
        this.location.setImageWidth(image.getWidth());
        this.location.setImageHeight(image.getHeight());
        // Check if the skin exceeds the set limits
        final Resolution max = this.addonContext.getAddonConfig().getMaxSkinResolution();
        if (max != Resolution.RESOLUTION_ALL && image.getHeight() > max.getHeight() && image.getWidth() > max.getWidth()) {
          LOGGER.debug("Not loading skin {} because it exceeds configured resolution limits: {}", this.texture.getHash(), max);
          this.skinManager.invalidateSkins(this.skinManager.findAssociatedUniqueIds((HDMinecraftProfileTexture) this.texture));
          ConcurrentUtils.callOnClientThread(ConcurrentUtils.runnableToCallable(this.backingLoaderExecutor));
          return;
        }

        ConcurrentUtils.callOnClientThread(ConcurrentUtils.runnableToCallable(() -> {
          this.textureManager.loadTexture(this.location, new HDSkinTexture(image));
          if (this.callback != null) {
            this.callback.skinAvailable(this.type, this.location, this.texture);
          }
        }));
      } catch (IOException exception) {
        LOGGER.debug(
          "Unable to load skin of texture: {} type: {} callback: {} path: {}",
          this.texture,
          this.type,
          this.callback,
          this.targetLocalPath,
          exception
        );
        ConcurrentUtils.callOnClientThread(ConcurrentUtils.runnableToCallable(this.backingLoaderExecutor));
      }
    } else {
      ConcurrentUtils.callOnClientThread(ConcurrentUtils.runnableToCallable(this.backingLoaderExecutor));
    }
  }
}
