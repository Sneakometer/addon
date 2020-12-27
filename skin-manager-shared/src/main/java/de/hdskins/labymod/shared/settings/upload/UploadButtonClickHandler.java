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
package de.hdskins.labymod.shared.settings.upload;

import com.google.common.collect.ImmutableSet;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.gui.AcceptRejectGuiScreen;
import de.hdskins.labymod.shared.notify.NotificationUtil;
import de.hdskins.labymod.shared.settings.countdown.ButtonCountdownElementNameChanger;
import de.hdskins.labymod.shared.settings.countdown.SettingsCountdownRegistry;
import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import de.hdskins.labymod.shared.utils.AwtUtils;
import de.hdskins.labymod.shared.utils.GuidelineUtils;
import net.labymod.utils.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

@ParametersAreNonnullByDefault
public class UploadButtonClickHandler implements Consumer<ButtonElement>, Constants {

  private static final int MAX_FILE_SIZE = 4 * 1024 * 1024;

  private static final FileDialog CHOOSER = new FileDialog((Frame) null, "Select file");
  private static final Set<String> ACCEPTED_EXTENSIONS = ImmutableSet.of("jpeg", "jpg", "png", "bmp", "webmp");
  private static final FilenameFilter FILTER = new SimpleFilenameFilter(ACCEPTED_EXTENSIONS);

  private final UploadFutureListener uploadFutureListener;
  private final AddonContext addonContext;

  public UploadButtonClickHandler(AddonContext addonContext) {
    this.uploadFutureListener = new UploadFutureListener(addonContext);
    this.addonContext = addonContext;
  }

  @Override
  public void accept(ButtonElement buttonElement) {
    if (CHOOSER.isVisible()) {
      CHOOSER.toFront();
      CHOOSER.requestFocus();
    } else {
      EXECUTOR.execute(() -> {
        final File result = AwtUtils.openFileChooser(CHOOSER, FILTER);
        CHOOSER.dispose(); // free native screen resources
        if (result == null || !result.exists() || result.isDirectory()) {
          NotificationUtil.notify(Constants.FAILURE, this.addonContext.getTranslationRegistry().translateMessage("change-skin-not-file"));
          return;
        }
        this.handleApprove(buttonElement, result);
      });
    }
  }

  private void handleApprove(ButtonElement buttonElement, File file) {
    if (!this.addonContext.getAddonConfig().hasAcceptedGuidelines()) {
      Collection<String> lines = GuidelineUtils.readGuidelines(this.addonContext.getAddonConfig().getGuidelinesUrl());
      AcceptRejectGuiScreen.newScreen(
        "Accept", "Decline",
        lines,
        (screen, accepted) -> {
          if (accepted) {
            this.addonContext.getAddonConfig().setGuidelinesAccepted(true);
            this.handleApprove(buttonElement, file);
          }
          screen.returnBack();
        }
      ).requestFocus();
      return;
    }

    ImageCheckResult result = this.processImage(file);
    if (result == ImageCheckResult.OK) {
      if (this.addonContext.getRateLimits().getUploadSkinRateLimit() > 0) {
        SettingsCountdownRegistry.registerTask(
          new ButtonCountdownElementNameChanger(buttonElement),
          this.addonContext.getRateLimits().getUploadSkinRateLimit()
        );
      }

      AddonContext.ServerResult serverResult = this.addonContext.uploadSkin(file);
      if (serverResult.getExecutionStage() != AddonContext.ExecutionStage.EXECUTING) {
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("change-skin-upload-failed-unknown"));
      } else {
        serverResult.getFuture().addListener(this.uploadFutureListener);
      }

      return;
    }

    switch (result) {
      case NOT_PNG:
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("change-skin-file-not-png"));
        break;
      case WRONG_PROPORTIONS:
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("change-skin-file-wrong-proportions"));
        break;
      case NOT_HD:
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("change-skin-file-not-hd"));
        break;
      case TOO_BIG:
        NotificationUtil.notify(FAILURE, this.addonContext.getTranslationRegistry().translateMessage("change-skin-file-too-large"));
        break;
      default:
        break;
    }
  }

  private ImageCheckResult processImage(File file) {
    if (file.length() > MAX_FILE_SIZE) {
      return ImageCheckResult.TOO_BIG;
    }

    try (InputStream inputStream = new FileInputStream(file); ImageInputStream stream = ImageIO.createImageInputStream(inputStream)) {
      Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(stream);
      if (!imageReaderIterator.hasNext()) {
        return ImageCheckResult.NOT_PNG;
      }

      ImageReader imageReader = imageReaderIterator.next();
      imageReader.setInput(stream);
      if (!imageReader.getFormatName().equals("png")) {
        return ImageCheckResult.NOT_PNG;
      }

      BufferedImage bufferedImage = imageReader.read(0);
      if (bufferedImage == null) {
        return ImageCheckResult.NOT_PNG;
      }

      if (bufferedImage.getHeight() <= 32 || bufferedImage.getWidth() <= 64) {
        return ImageCheckResult.NOT_HD;
      }

      if (bufferedImage.getHeight() != bufferedImage.getWidth() && bufferedImage.getHeight() != bufferedImage.getWidth() / 2) {
        return ImageCheckResult.WRONG_PROPORTIONS;
      }

      return ImageCheckResult.OK;
    } catch (IOException exception) {
      return ImageCheckResult.NOT_PNG;
    }
  }

  private enum ImageCheckResult {

    NOT_PNG,
    NOT_HD,
    WRONG_PROPORTIONS,
    TOO_BIG,
    OK
  }
}
