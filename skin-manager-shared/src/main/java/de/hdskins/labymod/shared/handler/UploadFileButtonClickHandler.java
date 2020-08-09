package de.hdskins.labymod.shared.handler;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class UploadFileButtonClickHandler implements Runnable {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    public UploadFileButtonClickHandler(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;

    private File lastUsedDirectory;
    private JFileChooser currentChooser;

    @Override
    public void run() {
        if (this.currentChooser == null) {
            Constants.EXECUTOR.execute(() -> {
                this.currentChooser = new JFileChooser(this.lastUsedDirectory);
                if (this.currentChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    UploadFileButtonClickHandler.this.handleApprove(this.currentChooser);
                }

                this.lastUsedDirectory = this.currentChooser.getCurrentDirectory();
                this.currentChooser = null;
            });
        } else {
            this.currentChooser.requestFocus();
        }
    }

    private void handleApprove(JFileChooser chooser) {
        try (InputStream inputStream = new FileInputStream(chooser.getSelectedFile())) {
            ImageCheckResult imageCheckResult = this.isPngImage(inputStream);
            if (imageCheckResult != ImageCheckResult.OK) {
                this.minecraftAdapter.changeToIngame();
                if (imageCheckResult == ImageCheckResult.NOT_PNG) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-file-not-png"));
                } else if (imageCheckResult == ImageCheckResult.NOT_HD) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-file-not-hd"));
                } else if (imageCheckResult == ImageCheckResult.WRONG_PROPORTIONS) {
                    this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-file-wrong-proportions"));
                }

                return;
            }

            if (chooser.getSelectedFile().length() > MAX_FILE_SIZE) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-file-too-large"));
                return;
            }

            StatusCode status = ServerHelper.uploadToServer(chooser.getSelectedFile().toPath(), this.minecraftAdapter, this.configObject);
            if (status == StatusCode.CREATED) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-upload-completed"));
            } else if (status == StatusCode.TOO_MANY_REQUESTS) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-rate-limited"));
            } else {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-upload-failed-unknown", status));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private ImageCheckResult isPngImage(InputStream inputStream) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(inputStream)) {
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

            if (bufferedImage.getHeight() <= 64 || bufferedImage.getWidth() <= 64) {
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
        OK
    }
}
