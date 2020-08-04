package de.hdskins.labymod.shared.elements;

import com.github.derklaro.requestbuilder.result.http.StatusCode;
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

public class UploadFileButtonClickHandler implements Runnable {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    public UploadFileButtonClickHandler(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        this.minecraftAdapter = minecraftAdapter;
        this.configObject = configObject;
    }

    private final MinecraftAdapter minecraftAdapter;
    private final ConfigObject configObject;

    @Override
    public void run() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (InputStream inputStream = new FileInputStream(chooser.getSelectedFile())) {
                if (!this.isPngImage(inputStream)) {
                    this.drawString("§cThe provided file is not an png image");
                    return;
                }

                if (chooser.getSelectedFile().length() > MAX_FILE_SIZE) {
                    this.drawString("§cThe provided file is too large. Maximum is to 2MB.");
                    return;
                }

                StatusCode status = ServerHelper.uploadToServer(chooser.getSelectedFile().toPath(), this.minecraftAdapter, this.configObject);
                if (status == StatusCode.CREATED) {
                    this.drawString("§aUploaded successfully completed");
                } else {
                    this.drawString("§cUpload failed with status " + status);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
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

    private void drawString(String text) {
        this.minecraftAdapter.drawString(text, this.minecraftAdapter.getWidth() / 2D, 35, 1.2);
    }
}
