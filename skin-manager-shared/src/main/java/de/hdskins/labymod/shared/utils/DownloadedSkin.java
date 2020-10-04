package de.hdskins.labymod.shared.utils;

import java.awt.image.BufferedImage;

public class DownloadedSkin {

    private final BufferedImage image;
    private final boolean slim;

    public DownloadedSkin(BufferedImage image, boolean slim) {
        this.image = image;
        this.slim = slim;
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean isSlim() {
        return slim;
    }
}
