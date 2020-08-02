package de.hdskins.labymod;

import de.hdskins.labymod.manager.SkinManagerInjector;
import de.hdskins.labymod.gui.ButtonElement;
import de.hdskins.labymod.gui.HdSkinManageElement;
import de.hdskins.labymod.utils.ServerHelper;
import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HdSkinsAddon extends LabyModAddon {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    public void onEnable() {
        SkinManagerInjector.setNewSkinManager();
    }

    @Override
    public void loadConfig() {
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new HdSkinManageElement());
        ButtonElement buttonElement = new ButtonElement("Change skin", new ControlElement.IconData(Material.PAINTING), null);
        buttonElement.setDescriptionText("Skins must not contain right-wing, narcissistic, offensive or sexual content.");
        buttonElement.setClickListener(() -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try (InputStream inputStream = new FileInputStream(chooser.getSelectedFile())) {
                    if (ImageIO.read(inputStream) == null) {
                        // Not a picture LOL
                        Minecraft.getMinecraft().getSession().getSessionID();
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
        });

        list.add(buttonElement);
    }

}