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
package de.hdskins.labymod.shared.texture;

import de.hdskins.labymod.shared.concurrent.ConcurrentUtil;
import de.hdskins.labymod.shared.utils.MCUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HDSkinTexture extends SimpleTexture {

  // initialized by class call
  protected final BufferedImage bufferedImage;
  // current values
  protected boolean blur;
  protected boolean mipmap;
  // last values
  protected boolean lastBlur;
  protected boolean lastMipmap;
  // lazy initialized
  protected int glTextureId = -1;

  public HDSkinTexture(Path skinPath) throws IOException {
    super(null); // we override all methods so no need to actually hand it over
    try (InputStream inputStream = Files.newInputStream(skinPath)) {
      this.bufferedImage = ImageIO.read(inputStream);
    }
  }

  public HDSkinTexture(BufferedImage bufferedImage) {
    super(null); // we override all methods so no need to actually hand it over
    this.bufferedImage = bufferedImage;
  }

  @Override
  public void setBlurMipmap(boolean blur, boolean mipmap) {
    // back these up for #restoreLastBlurMipmap()
    this.lastBlur = this.blur;
    this.lastMipmap = this.mipmap;
    // now change the current values
    this.blur = blur;
    this.mipmap = mipmap;
    // Ensure that we call this on the main thread because it is the only thread in
    // the client which has a opengl context
    MCUtil.call(ConcurrentUtil.fromRunnable(() -> {
      GL11.glTexParameteri(3553, 10241, blur ? mipmap ? 9987 : 9729 : mipmap ? 9986 : 9728);
      GL11.glTexParameteri(3553, 10240, blur ? 9729 : 9728);
    }));
  }

  @Override
  public void setBlurMipmapDirect(boolean blur, boolean mipmap) {
    this.setBlurMipmap(blur, mipmap);
  }

  @Override
  public void restoreLastBlurMipmap() {
    this.setBlurMipmap(this.lastBlur, this.lastMipmap);
  }

  @Override
  public void loadTexture(IResourceManager resourceManager) {
    // there is nothing to load
  }

  @Override
  public int getGlTextureId() {
    if (this.glTextureId == -1) {
      // We lazily set this up to spare the resources we don't need for later. We have to
      // ensure that we call this on the main thread because it is the only thread in
      // the client which has a opengl context
      this.glTextureId = MCUtil.call(() -> TextureUtil.uploadTextureImage(GL11.glGenTextures(), this.bufferedImage));
    }

    return this.glTextureId;
  }

  @Override
  public void deleteGlTexture() {
    // Check if the texture id is initialized so we can actually delete it
    if (this.glTextureId != -1) {
      // Ensure that we call this on the main thread because it is the only thread in
      // the client which has a opengl context
      MCUtil.call(ConcurrentUtil.fromRunnable(() -> GlStateManager.deleteTexture(this.glTextureId)));
      // Reset the texture id to indicate that we have to recalculate it
      this.glTextureId = -1;
    }
  }

  public BufferedImage getBufferedImage() {
    return this.bufferedImage;
  }
}
