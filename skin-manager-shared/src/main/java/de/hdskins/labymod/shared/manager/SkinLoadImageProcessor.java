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

import de.hdskins.labymod.shared.utils.ImageUtils;

import javax.annotation.Nonnull;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;

public final class SkinLoadImageProcessor {

  private static final int TRANSPARENT_FLAG = 0xffffff;
  private static final int OPAQUE_FLAG = 0xff000000;

  private SkinLoadImageProcessor() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public static BufferedImage process(@Nonnull InputStream stream) throws IOException {
    BufferedImage in = ImageUtils.readPngImage(stream);
    BufferedImage transform = new BufferedImage(in.getWidth(), in.getWidth(), BufferedImage.TYPE_INT_ARGB);
    process(in, transform);
    return transform;
  }

  private static void process(@Nonnull BufferedImage in, @Nonnull BufferedImage i) {
    final Graphics graphics = i.getGraphics();
    graphics.drawImage(in, 0, 0, null);
    if (in.getWidth() != in.getHeight()) {
      // convert from legacy format (width * width / 2) to modern format (width * width)
      graphics.drawImage(i, x(in, 24), y(in, 48), x(in, 20), y(in, 52), x(in, 4), y(in, 16), x(in, 8), y(in, 20), null);
      graphics.drawImage(i, x(in, 28), y(in, 48), x(in, 24), y(in, 52), x(in, 8), y(in, 16), x(in, 12), y(in, 20), null);
      graphics.drawImage(i, x(in, 20), y(in, 52), x(in, 16), y(in, 64), x(in, 8), y(in, 20), x(in, 12), y(in, 32), null);
      graphics.drawImage(i, x(in, 24), y(in, 52), x(in, 20), y(in, 64), x(in, 4), y(in, 20), x(in, 8), y(in, 32), null);
      graphics.drawImage(i, x(in, 28), y(in, 52), x(in, 24), y(in, 64), x(in, 0), y(in, 20), x(in, 4), y(in, 32), null);
      graphics.drawImage(i, x(in, 32), y(in, 52), x(in, 28), y(in, 64), x(in, 12), y(in, 20), x(in, 16), y(in, 32), null);
      graphics.drawImage(i, x(in, 40), y(in, 48), x(in, 36), y(in, 52), x(in, 44), y(in, 16), x(in, 48), y(in, 20), null);
      graphics.drawImage(i, x(in, 44), y(in, 48), x(in, 40), y(in, 52), x(in, 48), y(in, 16), x(in, 52), y(in, 20), null);
      graphics.drawImage(i, x(in, 36), y(in, 52), x(in, 32), y(in, 64), x(in, 48), y(in, 20), x(in, 52), y(in, 32), null);
      graphics.drawImage(i, x(in, 40), y(in, 52), x(in, 36), y(in, 64), x(in, 44), y(in, 20), x(in, 48), y(in, 32), null);
      graphics.drawImage(i, x(in, 44), y(in, 52), x(in, 40), y(in, 64), x(in, 40), y(in, 20), x(in, 44), y(in, 32), null);
      graphics.drawImage(i, x(in, 48), y(in, 52), x(in, 44), y(in, 64), x(in, 52), y(in, 20), x(in, 56), y(in, 32), null);
    }
    graphics.dispose();
    processDataBuffer(i, ((DataBufferInt) i.getRaster().getDataBuffer()).getData());
  }

  private static void processDataBuffer(@Nonnull BufferedImage image, int[] dataBuffer) {
    setAreaOpaque(dataBuffer, image.getWidth(), 0, 0, x(image, 32), y(image, 16));
    setAreaTransparent(dataBuffer, image.getWidth(), x(image, 32), 0, x(image, 64), y(image, 32));
    setAreaOpaque(dataBuffer, image.getWidth(), 0, y(image, 16), x(image, 64), y(image, 32));
    setAreaTransparent(dataBuffer, image.getWidth(), 0, y(image, 32), x(image, 16), y(image, 48));
    setAreaTransparent(dataBuffer, image.getWidth(), x(image, 16), y(image, 32), x(image, 40), y(image, 48));
    setAreaTransparent(dataBuffer, image.getWidth(), x(image, 40), y(image, 32), x(image, 56), y(image, 48));
    setAreaTransparent(dataBuffer, image.getWidth(), 0, y(image, 48), x(image, 16), y(image, 64));
    setAreaOpaque(dataBuffer, image.getWidth(), x(image, 16), y(image, 48), x(image, 48), y(image, 64));
    setAreaTransparent(dataBuffer, image.getWidth(), x(image, 48), y(image, 48), x(image, 64), y(image, 64));
  }

  private static int x(@Nonnull BufferedImage image, int in) {
    return in * (image.getWidth() / 64);
  }

  private static int y(@Nonnull BufferedImage image, int in) {
    return in * (image.getHeight() / (image.getHeight() != image.getWidth() ? 32 : 64));
  }

  private static void setAreaTransparent(int[] data, int width, int x, int y, int maxX, int maxY) {
    if (!hasTransparency(data, width, x, y, maxX, maxY)) {
      for (int currentX = x; currentX < maxX; ++currentX) {
        for (int currentY = y; currentY < maxY; ++currentY) {
          data[currentX + currentY * width] &= TRANSPARENT_FLAG;
        }
      }
    }
  }

  private static void setAreaOpaque(int[] data, int width, int x, int y, int maxX, int maxY) {
    for (int currentX = x; currentX < maxX; ++currentX) {
      for (int currentY = y; currentY < maxY; ++currentY) {
        data[currentX + currentY * width] |= OPAQUE_FLAG;
      }
    }
  }

  private static boolean hasTransparency(int[] data, int width, int x, int y, int maxX, int maxY) {
    for (int currentX = x; currentX < maxX; ++currentX) {
      for (int currentY = y; currentY < maxY; ++currentY) {
        final int positionFlag = data[currentX + currentY * width];
        if ((positionFlag >> 24 & 255) < 128) {
          return true;
        }
      }
    }
    return false;
  }
}
