/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 - 2021 HD-Skins <https://github.com/HDSkins>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.hdskins.labymod.shared.settings;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.config.resolution.Resolution;
import de.hdskins.labymod.shared.settings.delete.DeleteButtonClickHandler;
import de.hdskins.labymod.shared.settings.element.ElementFactory;
import de.hdskins.labymod.shared.settings.eula.EulaButtonClickListener;
import de.hdskins.labymod.shared.settings.slim.SlimButtonClickHandler;
import de.hdskins.labymod.shared.settings.toggle.SkinToggleButtonClickHandler;
import de.hdskins.labymod.shared.settings.upload.UploadButtonClickHandler;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public final class SettingsFactory {

  public SettingsFactory() {
    throw new UnsupportedOperationException();
  }

  public static Collection<SettingsElement> bakeSettings(AddonContext addonContext) {
    SettingsElement setSlimElement = ElementFactory.defaultFactory().brewBooleanElement(
      addonContext.getTranslationRegistry().translateMessage("slim-skin-change-option"),
      new ControlElement.IconData(Material.REDSTONE_COMPARATOR),
      addonContext.getTranslationRegistry().translateMessage("slim-skin-option-on"),
      addonContext.getTranslationRegistry().translateMessage("slim-skin-option-off"),
      addonContext.getAddonConfig().isSlim(),
      new SlimButtonClickHandler(addonContext),
      element -> {
        element.setDescriptionText(addonContext.getTranslationRegistry().translateMessage("slim-skin-option-description"));
      }
    );
    SettingsElement uploadSkinElement = ElementFactory.defaultFactory().brewButtonElement(
      addonContext.getTranslationRegistry().translateMessage("change-skin-option-name"),
      new ControlElement.IconData(Material.PAINTING),
      addonContext.getTranslationRegistry().translateMessage("button-click-here"),
      new UploadButtonClickHandler(addonContext),
      buttonElement -> {
        buttonElement.setDescriptionText(addonContext.getTranslationRegistry().translateMessage("change-skin-option-description"));
      }
    );
    SettingsElement deleteSkinElement = ElementFactory.defaultFactory().brewButtonElement(
      addonContext.getTranslationRegistry().translateMessage("delete-skin-option-name"),
      new ControlElement.IconData(Material.BARRIER),
      addonContext.getTranslationRegistry().translateMessage("button-click-here"),
      new DeleteButtonClickHandler(addonContext),
      buttonElement -> {
        buttonElement.setDescriptionText(addonContext.getTranslationRegistry().translateMessage("delete-skin-option-description"));
      }
    );
    SettingsElement toggleSkinVisibilityElement = ElementFactory.defaultFactory().brewBooleanElement(
      addonContext.getTranslationRegistry().translateMessage("show-all-skins"),
      new ControlElement.IconData(Material.SKULL_ITEM),
      addonContext.getTranslationRegistry().translateMessage("show-all-skins-option-on"),
      addonContext.getTranslationRegistry().translateMessage("show-all-skins-option-off"),
      addonContext.getAddonConfig().showSkinsOfOtherPlayers(),
      new SkinToggleButtonClickHandler(addonContext),
      element -> {
        element.setDescriptionText(addonContext.getTranslationRegistry().translateMessage("show-all-skins-description"));
      }
    );
    SettingsElement maxResolutionLoadOption = ElementFactory.defaultFactory().brewDropDownElement(
      addonContext.getTranslationRegistry().translateMessage("set-max-resolution-option-name"),
      new ControlElement.IconData(Material.RECORD_9),
      addonContext.getAddonConfig().getMaxSkinResolution().getName(),
      Arrays.stream(Resolution.VALUES).map(Resolution::getName).collect(Collectors.toList()),
      (ignored, newValue) -> Resolution.byName(newValue).ifPresent(addonContext::setMaxSkinResolution),
      dropDownElement -> {
        dropDownElement.setDescriptionText(addonContext.getTranslationRegistry().translateMessage("set-max-resolution-option-description"));
      }
    );
    SettingsElement eulaReadElement = ElementFactory.defaultFactory().brewEulaButtonElement(
      new EulaButtonClickListener(addonContext),
      element -> {
      });
    SettingsElement skinRenderElement = ElementFactory.defaultFactory().brewRenderElement(element -> {
    });

    return Arrays.asList(
      setSlimElement, uploadSkinElement,
      deleteSkinElement, toggleSkinVisibilityElement,
      maxResolutionLoadOption, skinRenderElement,
      eulaReadElement
    );
  }
}
