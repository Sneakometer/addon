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
            if (!this.isPngImage(inputStream)) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat(LanguageManager.getTranslation("change-skin-file-not-png"));
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

    private boolean isPngImage(InputStream inputStream) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(stream);
            if (!imageReaderIterator.hasNext()) {
                return false;
            }

            ImageReader imageReader = imageReaderIterator.next();
            imageReader.setInput(stream);
            if (!imageReader.getFormatName().equals("png")) {
                return false;
            }

            return imageReader.read(0) != null;
        } catch (IOException exception) {
            return false;
        }
    }
}
