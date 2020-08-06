package de.hdskins.labymod.shared.handler;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.ServerHelper;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class UploadFileButtonClickHandler implements Runnable {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    public UploadFileButtonClickHandler(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;

    private final AtomicBoolean chooserOpen = new AtomicBoolean();

    @Override
    public void run() {
        if (!chooserOpen.getAndSet(true)) {
            Constants.EXECUTOR.execute(() -> {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    UploadFileButtonClickHandler.this.handleApprove(chooser);
                }

                UploadFileButtonClickHandler.this.chooserOpen.set(false);
            });
        }
    }

    private void handleApprove(JFileChooser chooser) {
        try (InputStream inputStream = new FileInputStream(chooser.getSelectedFile())) {
            if (!this.isPngImage(inputStream)) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat("§cThe provided file is not a png image");
                return;
            }

            if (chooser.getSelectedFile().length() > MAX_FILE_SIZE) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat("§cThe provided file is too large. Maximum is to 2MB.");
                return;
            }

            StatusCode status = ServerHelper.uploadToServer(chooser.getSelectedFile().toPath(), this.minecraftAdapter, this.configObject);
            if (status == StatusCode.CREATED) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat("§aUpload successfully completed. It may take up to two minutes until the changes are active.");
            } else if (status == StatusCode.TOO_MANY_REQUESTS) {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat("§cYou can only upload a skin every two minutes");
            } else {
                this.minecraftAdapter.changeToIngame();
                this.minecraftAdapter.displayMessageInChat("§cUpload failed with status " + status);
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
