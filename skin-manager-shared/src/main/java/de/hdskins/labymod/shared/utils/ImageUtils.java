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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public final class ImageUtils {

  private static final ThreadLocal<ImageReader> PNG_READER = ThreadLocal.withInitial(ImageUtils::loadPngReader);

  private ImageUtils() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public static BufferedImage readPngImage(@Nonnull InputStream stream) throws IOException {
    final ImageReader pngReader = PNG_READER.get();
    if (pngReader != null) {
      return doFastRead(stream, pngReader);
    }
    return ImageIO.read(stream);
  }

  @Nonnull
  public static BufferedImage doFastRead(@Nonnull InputStream stream, @Nonnull ImageReader reader) throws IOException {
    try (ImageInputStream inputStream = new FileCacheImageInputStream(new BufferedInputStream(stream), ImageIO.getCacheDirectory())) {
      final ImageReadParam imageReadParam = reader.getDefaultReadParam();
      reader.setInput(inputStream, true, true);
      return reader.read(0, imageReadParam);
    } finally {
      reader.dispose();
    }
  }

  @Nullable
  private static ImageReader loadPngReader() {
    final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
    return readers.hasNext() ? readers.next() : null;
  }
}
