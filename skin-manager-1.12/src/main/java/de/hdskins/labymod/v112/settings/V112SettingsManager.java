package de.hdskins.labymod.v112.settings;

import de.hdskins.labymod.shared.callbacks.SlimElementChangeConsumer;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.gui.ButtonElement;
import de.hdskins.labymod.shared.handler.DeleteSkinButtonClickHandler;
import de.hdskins.labymod.shared.handler.UploadFileButtonClickHandler;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.v112.gui.V112BooleanElement;
import de.hdskins.labymod.v112.gui.V112ButtonElement;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.List;

public class V112SettingsManager {

    private ButtonElement uploadSkinElement;
    private ButtonElement deleteSkinElement;
    private BooleanElement slimElement;

    public void draw(MinecraftAdapter minecraftAdapter, List<SettingsElement> list, ConfigObject object, boolean slim) {
        this.uploadSkinElement = new V112ButtonElement(
                LanguageManager.getTranslation("change-skin-option-name"),
                new ControlElement.IconData(Material.PAINTING),
                LanguageManager.getTranslation("button-click-here")
        );
        uploadSkinElement.setDescriptionText(LanguageManager.getTranslation("change-skin-option-description"));
        uploadSkinElement.setClickListener(new UploadFileButtonClickHandler(minecraftAdapter, object));
        list.add(uploadSkinElement);

        this.deleteSkinElement = new V112ButtonElement(
                LanguageManager.getTranslation("delete-skin-option-name"),
                new ControlElement.IconData(Material.BARRIER),
                LanguageManager.getTranslation("button-click-here")
        );
        deleteSkinElement.setDescriptionText(LanguageManager.getTranslation("delete-skin-option-description"));
        deleteSkinElement.setClickListener(new DeleteSkinButtonClickHandler(minecraftAdapter, object));
        list.add(deleteSkinElement);

        this.slimElement = new V112BooleanElement(
                LanguageManager.getTranslation("slim-skin-change-option"), new ControlElement.IconData(Material.REDSTONE_COMPARATOR),
                LanguageManager.getTranslation("slim-skin-option-on"), LanguageManager.getTranslation("slim-skin-option-off"),
                slim, new SlimElementChangeConsumer(object, minecraftAdapter)
        );
        slimElement.setDescriptionText(LanguageManager.getTranslation("slim-skin-option-description"));
        list.add(slimElement);
    }

    public void redraw() {
        if (this.uploadSkinElement != null) {
            this.uploadSkinElement.setDisplayName(LanguageManager.getTranslation("change-skin-option-name"));
            this.uploadSkinElement.setText(LanguageManager.getTranslation("button-click-here"));
            this.uploadSkinElement.setDescriptionText(LanguageManager.getTranslation("change-skin-option-description"));
        }

        if (this.deleteSkinElement != null) {
            this.deleteSkinElement.setDisplayName(LanguageManager.getTranslation("delete-skin-option-name"));
            this.deleteSkinElement.setText(LanguageManager.getTranslation("button-click-here"));
            this.deleteSkinElement.setDescriptionText(LanguageManager.getTranslation("delete-skin-option-description"));
        }

        if (this.slimElement != null) {
            this.slimElement.custom(LanguageManager.getTranslation("slim-skin-option-on"), LanguageManager.getTranslation("slim-skin-option-off"));
            this.slimElement.setDisplayName(LanguageManager.getTranslation("slim-skin-change-option"));
            this.slimElement.setDescriptionText(LanguageManager.getTranslation("slim-skin-option-description"));
        }
    }

    public boolean shouldRedraw() {
        return this.uploadSkinElement != null && this.deleteSkinElement != null && this.slimElement != null;
    }
}
