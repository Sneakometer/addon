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

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
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
    BufferedImage in = ImageIO.read(stream);
    BufferedImage transform = new BufferedImage(in.getHeight(), in.getWidth(), BufferedImage.TYPE_INT_ARGB);
    process(in, transform);
    return transform;
  }

  private static void process(@Nonnull BufferedImage in, @Nonnull BufferedImage i) {
    final Graphics graphics = i.getGraphics();
    graphics.drawImage(in, 0, 0, null);
    if (i.getWidth() != i.getHeight()) {
      graphics.drawImage(i, x(i, 24), y(i, 48), x(i, 20), y(i, 52), x(i, 4), y(i, 16), x(i, 8), y(i, 20), null);
      graphics.drawImage(i, x(i, 28), y(i, 48), x(i, 24), y(i, 52), x(i, 8), y(i, 16), x(i, 12), y(i, 20), null);
      graphics.drawImage(i, x(i, 20), y(i, 52), x(i, 16), y(i, 64), x(i, 8), y(i, 20), x(i, 12), y(i, 32), null);
      graphics.drawImage(i, x(i, 24), y(i, 52), x(i, 20), y(i, 64), x(i, 4), y(i, 20), x(i, 8), y(i, 32), null);
      graphics.drawImage(i, x(i, 28), y(i, 52), x(i, 24), y(i, 64), x(i, 0), y(i, 20), x(i, 4), y(i, 32), null);
      graphics.drawImage(i, x(i, 32), y(i, 52), x(i, 28), y(i, 64), x(i, 12), y(i, 20), x(i, 16), y(i, 32), null);
      graphics.drawImage(i, x(i, 40), y(i, 48), x(i, 36), y(i, 52), x(i, 44), y(i, 16), x(i, 48), y(i, 20), null);
      graphics.drawImage(i, x(i, 44), y(i, 48), x(i, 40), y(i, 52), x(i, 48), y(i, 16), x(i, 52), y(i, 20), null);
      graphics.drawImage(i, x(i, 36), y(i, 52), x(i, 32), y(i, 64), x(i, 48), y(i, 20), x(i, 52), y(i, 32), null);
      graphics.drawImage(i, x(i, 40), y(i, 52), x(i, 36), y(i, 64), x(i, 44), y(i, 20), x(i, 48), y(i, 32), null);
      graphics.drawImage(i, x(i, 44), y(i, 52), x(i, 40), y(i, 64), x(i, 40), y(i, 20), x(i, 44), y(i, 32), null);
      graphics.drawImage(i, x(i, 48), y(i, 52), x(i, 44), y(i, 64), x(i, 52), y(i, 20), x(i, 56), y(i, 32), null);
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
    return in * (image.getHeight() / 64);
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
