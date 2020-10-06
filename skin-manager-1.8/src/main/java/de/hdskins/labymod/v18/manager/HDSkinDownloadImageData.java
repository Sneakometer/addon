package de.hdskins.labymod.v18.manager;

import de.hdskins.labymod.test.config.ConfigObject;
import de.hdskins.labymod.test.utils.DownloadedSkin;
import de.hdskins.labymod.test.utils.ServerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class HDSkinDownloadImageData extends SimpleTexture {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);

    private final ConfigObject config;
    private final File cacheFile;
    private final String mojangUrl;
    private final IImageBuffer imageBuffer;
    private final ProfiledMinecraftProfileTexture texture;
    private BufferedImage image;
    private Thread imageThread;
    private boolean textureUploaded;

    public HDSkinDownloadImageData(File cacheFile, String mojangUrl, ResourceLocation defaultImage /* the steve */, ConfigObject config, IImageBuffer buffer, ProfiledMinecraftProfileTexture texture) {
        super(defaultImage);
        this.cacheFile = cacheFile;
        this.mojangUrl = mojangUrl;
        this.config = config;
        this.imageBuffer = buffer;
        this.texture = texture;
    }

    private void checkTextureUploaded() {
        if (!this.textureUploaded) {
            if (this.image != null) {
                if (this.textureLocation != null) {
                    this.deleteGlTexture();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.image);
                this.textureUploaded = true;
            }
        }
    }

    public int getGlTextureId() {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        if (this.imageBuffer != null) {
            this.imageBuffer.skinAvailable();
        }
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        if (this.image == null && this.textureLocation != null) {
            super.loadTexture(resourceManager);
        }

        if (this.imageThread == null) {
            if (this.cacheFile != null && this.cacheFile.isFile()) {
                LOGGER.debug("Loading http texture from local cache ({})", this.cacheFile);

                try {
                    this.image = ImageIO.read(this.cacheFile);
                    if (this.imageBuffer != null) {
                        this.setImage(this.imageBuffer.parseUserSkin(this.image));
                    }
                } catch (IOException var3) {
                    LOGGER.error("Couldn't load skin " + this.cacheFile, var3);
                    this.loadTextureFromServer();
                }
            } else {
                this.loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer() {
        this.imageThread = new Thread(() -> {
            DownloadedSkin skin = ServerHelper.downloadSkin(this.config, this.texture.getProfile().getId().toString().replace("-", ""));
            if (skin != null) {
                if (skin.isSlim()) {
                    this.texture.getMetadata().put("model", "slim");
                } else {
                    this.texture.getMetadata().remove("model");
                }
                this.setImage(skin.getImage());
                return;
            }

            this.downloadOriginalSkin();
        }, "Texture Downloader #" + threadDownloadCounter.incrementAndGet());
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }

    private void downloadOriginalSkin() {
        HttpURLConnection connection = null;
        LOGGER.debug("Downloading http texture from {} to {}", this.mojangUrl, this.cacheFile);

        try {
            connection = (HttpURLConnection) new URL(this.mojangUrl).openConnection(Minecraft.getMinecraft().getProxy());
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                BufferedImage image;
                if (this.cacheFile != null) {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), this.cacheFile);
                    image = ImageIO.read(this.cacheFile);
                } else {
                    image = TextureUtil.readBufferedImage(connection.getInputStream());
                }

                if (this.imageBuffer != null) {
                    image = this.imageBuffer.parseUserSkin(image);
                }

                this.setImage(image);
            }
        } catch (Exception var6) {
            LOGGER.error("Couldn't download http texture", var6);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
