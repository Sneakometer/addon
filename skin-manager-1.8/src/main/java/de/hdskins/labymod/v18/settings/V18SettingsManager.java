package de.hdskins.labymod.v18.settings;

import de.hdskins.labymod.test.callbacks.ShowSkinElementChangeConsumer;
import de.hdskins.labymod.test.callbacks.SlimElementChangeConsumer;
import de.hdskins.labymod.test.config.ConfigObject;
import de.hdskins.labymod.test.gui.ButtonElement;
import de.hdskins.labymod.test.gui.SkinRenderElement;
import de.hdskins.labymod.test.handler.DeleteSkinButtonClickHandler;
import de.hdskins.labymod.test.handler.UploadFileButtonClickHandler;
import de.hdskins.labymod.test.language.LanguageManager;
import de.hdskins.labymod.test.minecraft.MinecraftAdapter;
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
    private V18BooleanElement showSkinsElement;
    private V18BooleanElement slimElement;

    public void draw(MinecraftAdapter minecraftAdapter, List<SettingsElement> list, ConfigObject object, boolean slim) {
        this.slimElement = new V18BooleanElement(
                LanguageManager.getTranslation("slim-skin-change-option"), new ControlElement.IconData(Material.REDSTONE_COMPARATOR),
                LanguageManager.getTranslation("slim-skin-option-on"), LanguageManager.getTranslation("slim-skin-option-off"),
                slim, new SlimElementChangeConsumer(object, minecraftAdapter)
        );
        this.slimElement.setDescriptionText(LanguageManager.getTranslation("slim-skin-option-description"));

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
        deleteSkinElement.setClickListener(new DeleteSkinButtonClickHandler(minecraftAdapter, object, this.slimElement));
        list.add(deleteSkinElement);

        list.add(this.slimElement);

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

        this.showSkinsElement = new V18BooleanElement(
                LanguageManager.getTranslation("show-all-skins"),
                new ControlElement.IconData(Material.SKULL_ITEM),
                LanguageManager.getTranslation("show-all-skins-option-on"), LanguageManager.getTranslation("show-all-skins-option-off"),
                slim, new ShowSkinElementChangeConsumer(minecraftAdapter)
        ) {
            @Override
            public boolean getCurrentValue() {
                return minecraftAdapter.getConfig().shouldShowAllSkins();
            }
        };
        this.showSkinsElement.setDescriptionText(LanguageManager.getTranslation("show-all-skins-description"));
        list.add(this.showSkinsElement);

        list.add(new SkinRenderElement(minecraftAdapter));
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
            this.slimElement.setDisplayName(LanguageManager.getTranslation("slim-skin-change-option"));
            this.slimElement.setDescriptionText(LanguageManager.getTranslation("slim-skin-option-description"));
            this.slimElement.custom(LanguageManager.getTranslation("slim-skin-option-on"), LanguageManager.getTranslation("slim-skin-option-off"));
        }

        if (this.refreshCacheElement != null) {
            this.refreshCacheElement.setDisplayName(LanguageManager.getTranslation("refresh-skin-cache"));
            this.refreshCacheElement.setText(LanguageManager.getTranslation("button-click-here"));
            this.refreshCacheElement.setDescriptionText(LanguageManager.getTranslation("refresh-skin-cache-description"));
        }

        if (this.showSkinsElement != null) {
            this.showSkinsElement.setDisplayName(LanguageManager.getTranslation("show-all-skins"));
            this.showSkinsElement.setDescriptionText(LanguageManager.getTranslation("show-all-skins"));
            this.showSkinsElement.custom(LanguageManager.getTranslation("show-all-skins-option-on"), LanguageManager.getTranslation("show-all-skins-option-off"));
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
