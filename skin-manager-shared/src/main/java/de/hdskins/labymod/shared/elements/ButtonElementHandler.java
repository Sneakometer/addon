package de.hdskins.labymod.shared.elements;

import de.hdskins.labymod.shared.utils.ServerHelper;
import net.labymod.main.LabyMod;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ButtonElementHandler implements Runnable {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    public void run() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (InputStream inputStream = new FileInputStream(chooser.getSelectedFile())) {
                if (ImageIO.read(inputStream) == null) {
                    // Not a picture LOL
                    LabyMod.getInstance().displayMessageInChat("Not a picture you lelek");
                    return;
                }

                if (chooser.getSelectedFile().length() > MAX_FILE_SIZE) {
                    LabyMod.getInstance().displayMessageInChat(chooser.getSelectedFile().length() + ">" + MAX_FILE_SIZE);
                    return;
                }

                LabyMod.getInstance().displayMessageInChat("Uploading....");
                LabyMod.getInstance().displayMessageInChat("Upload done! Result: " + ServerHelper.uploadToServer(inputStream).name());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
