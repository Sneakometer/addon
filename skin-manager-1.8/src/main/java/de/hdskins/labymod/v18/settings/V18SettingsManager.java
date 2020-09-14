package de.hdskins.labymod.v18.settings;

import de.hdskins.labymod.shared.callbacks.SlimElementChangeConsumer;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.gui.ButtonElement;
import de.hdskins.labymod.shared.gui.SkinRenderElement;
import de.hdskins.labymod.shared.handler.DeleteSkinButtonClickHandler;
import de.hdskins.labymod.shared.handler.UploadFileButtonClickHandler;
import de.hdskins.labymod.shared.language.LanguageManager;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.utils.Side;
import de.hdskins.labymod.v18.gui.V18BooleanElement;
import de.hdskins.labymod.v18.gui.V18ButtonElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.List;

public class V18SettingsManager {

    private ButtonElement uploadSkinElement;
    private ButtonElement deleteSkinElement;
    private ButtonElement refreshCacheElement;
    private V18BooleanElement slimElement;

    public void draw(MinecraftAdapter minecraftAdapter, List<SettingsElement> list, ConfigObject object, boolean slim) {
        this.uploadSkinElement = new V18ButtonElement(
                LanguageManager.getTranslation("change-skin-option-name"),
                new ControlElement.IconData(Material.PAINTING),
                LanguageManager.getTranslation("button-click-here")
        );
        uploadSkinElement.setDescriptionText(LanguageManager.getTranslation("change-skin-option-description"));
        uploadSkinElement.setClickListener(new UploadFileButtonClickHandler(minecraftAdapter, object));
        list.add(uploadSkinElement);

        this.deleteSkinElement = new V18ButtonElement(
                LanguageManager.getTranslation("delete-skin-option-name"),
                new ControlElement.IconData(Material.BARRIER),
                LanguageManager.getTranslation("button-click-here")
        );
        deleteSkinElement.setDescriptionText(LanguageManager.getTranslation("delete-skin-option-description"));
        deleteSkinElement.setClickListener(new DeleteSkinButtonClickHandler(minecraftAdapter, object));
        list.add(deleteSkinElement);

        this.slimElement = new V18BooleanElement(
                LanguageManager.getTranslation("slim-skin-change-option"), new ControlElement.IconData(Material.REDSTONE_COMPARATOR),
                LanguageManager.getTranslation("slim-skin-option-on"), LanguageManager.getTranslation("slim-skin-option-off"),
                slim, new SlimElementChangeConsumer(object, minecraftAdapter)
        );
        slimElement.setDescriptionText(LanguageManager.getTranslation("slim-skin-option-description"));
        list.add(slimElement);

        this.refreshCacheElement = new V18ButtonElement(
                LanguageManager.getTranslation("refresh-skin-cache"),
                new ControlElement.IconData(Material.ARROW),
                LanguageManager.getTranslation("button-click-here")
        );
        this.refreshCacheElement.setDescriptionText(LanguageManager.getTranslation("refresh-skin-cache-description"));
        this.refreshCacheElement.setClickListener(() -> {
            minecraftAdapter.invalidateSkinCache();
            minecraftAdapter.changeToIngame();
        });
        list.add(this.refreshCacheElement);

        list.add(new SkinRenderElement(minecraftAdapter, Side.RIGHT));
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

        if (this.refreshCacheElement != null) {
            this.refreshCacheElement.setDisplayName(LanguageManager.getTranslation("refresh-skin-cache"));
            this.refreshCacheElement.setText(LanguageManager.getTranslation("button-click-here"));
            this.refreshCacheElement.setDescriptionText(LanguageManager.getTranslation("refresh-skin-cache-description"));
        }
    }

    public boolean shouldRedraw() {
        return this.uploadSkinElement != null && this.deleteSkinElement != null && this.slimElement != null;
    }

    public void setSlim(boolean slim) {
        if (this.slimElement == null) {
            return;
        }

        this.slimElement.setCurrentValue(slim);
    }

}
