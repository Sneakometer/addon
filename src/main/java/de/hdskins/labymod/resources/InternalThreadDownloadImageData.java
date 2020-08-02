package de.hdskins.labymod.resources;

import de.hdskins.labymod.manager.HDSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class InternalThreadDownloadImageData extends SimpleTexture {

    private final File cacheFile;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private final CompletableFuture<Boolean> future;
    private long loaded;
    private BufferedImage bufferedImage;
    private boolean textureUploaded;

    public InternalThreadDownloadImageData(File cacheFileIn, String imageUrlIn,
                                           ResourceLocation textureResourceLocation,
                                           IImageBuffer imageBufferIn, CompletableFuture<Boolean> future) {
        super(textureResourceLocation);
        this.cacheFile = cacheFileIn;
        this.imageUrl = imageUrlIn;
        this.imageBuffer = imageBufferIn;
        this.future = future;
    }

    private void checkTextureUploaded() {
        if (!this.textureUploaded && this.bufferedImage != null) {
            if (this.textureLocation != null) {
                this.deleteGlTexture();
            }

            TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
            this.textureUploaded = true;
        }
    }

    @Override
    public int getGlTextureId() {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn) {
        this.bufferedImage = bufferedImageIn;

        if (this.imageBuffer != null) {
            this.imageBuffer.skinAvailable();
        }

        if (this.future != null && !this.future.isDone()) {
            this.future.complete(bufferedImageIn != null);
        }
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        try {
            if (this.bufferedImage == null && this.textureLocation != null) {
                super.loadTexture(resourceManager);
            }

            if (true) {//this.loaded == 0) {
                if (false) {//this.cacheFile != null && this.cacheFile.isFile()) {
                    try {
                        this.bufferedImage = ImageIO.read(this.cacheFile);
                        if (this.imageBuffer != null) {
                            this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
                        }
                    } catch (IOException ioexception) {
                        ioexception.printStackTrace();
                        this.loadTextureFromServer();
                    }
                } else {
                    this.loadTextureFromServer();
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected void loadTextureFromServer() {
        this.loaded = System.currentTimeMillis();
        HDSkinManager.THREAD_POOL.execute(() -> {
            HttpURLConnection httpurlconnection = null;

            try {
                httpurlconnection = (HttpURLConnection) new URL(this.imageUrl).openConnection(Minecraft.getMinecraft().getProxy());
                httpurlconnection.setConnectTimeout(4000);
                httpurlconnection.setReadTimeout(10000);
                httpurlconnection.setDoInput(true);
                httpurlconnection.setDoOutput(false);
                httpurlconnection.connect();

                if (httpurlconnection.getResponseCode() == 200) {
                    BufferedImage bufferedimage;

                    if (this.cacheFile != null) {
                        FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), this.cacheFile);
                        bufferedimage = ImageIO.read(this.cacheFile);
                    } else {
                        bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
                    }

                    if (this.imageBuffer != null) {
                        bufferedimage = this.imageBuffer.parseUserSkin(bufferedimage);
                    }

                    this.setBufferedImage(bufferedimage);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                if (httpurlconnection != null) {
                    httpurlconnection.disconnect();
                }
            }
        });
    }
}
