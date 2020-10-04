package de.hdskins.labymod.v18.manager;

import net.minecraft.client.renderer.IImageBuffer;

import java.awt.image.BufferedImage;

public class WrappedImageBuffer implements IImageBuffer {

    private final IImageBuffer buffer;
    private final Runnable availableHandler;

    public WrappedImageBuffer(IImageBuffer buffer, Runnable availableHandler) {
        this.buffer = buffer;
        this.availableHandler = availableHandler;
    }

    @Override
    public BufferedImage parseUserSkin(BufferedImage image) {
        if (this.buffer != null) {
            return this.buffer.parseUserSkin(image);
        }

        return image;
    }

    @Override
    public void skinAvailable() {
        if (this.buffer != null) {
            this.buffer.skinAvailable();
        }

        if (this.availableHandler != null) {
            this.availableHandler.run();
        }
    }

}
