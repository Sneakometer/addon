package de.hdskins.labymod;

import de.hdskins.labymod.gui.HdSkinManageElement;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

public class HdSkinsAddon extends LabyModAddon {

    @Override
    public void onEnable() {
    }

    @Override
    public void loadConfig() {

    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new HdSkinManageElement());
    }

}