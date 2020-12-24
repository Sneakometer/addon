/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
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
package de.hdskins.labymod.shared.settings.element;

import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import de.hdskins.labymod.shared.settings.element.elements.ChangeableBooleanElement;
import de.hdskins.labymod.shared.settings.element.elements.CustomDropDownElement;
import de.hdskins.labymod.shared.settings.element.elements.PlayerSkinRenderElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@ParametersAreNonnullByDefault
class DefaultElementFactory implements ElementFactory {

  protected static final ElementFactory DEFAULT = new DefaultElementFactory();
  private boolean settingsEnabledByDefault = true;

  private DefaultElementFactory() {
  }

  @Nonnull
  @Override
  public ChangeableBooleanElement brewBooleanElement(String displayName, ControlElement.IconData iconData, String on, String off, boolean currentValue,
                                                     BiFunction<ChangeableBooleanElement, Boolean, CompletableFuture<Boolean>> toggleListener, Consumer<ChangeableBooleanElement> customizer) {
    ChangeableBooleanElement element = new ChangeableBooleanElement(displayName, iconData, on, off, currentValue, toggleListener);
    element.setSettingEnabled(this.settingsEnabledByDefault);
    customizer.accept(element);
    return element;
  }

  @Nonnull
  @Override
  public <T> DropDownElement<T> brewDropDownElement(String displayName, ControlElement.IconData iconData, T initialValue, List<T> values, BiConsumer<DropDownElement<T>, T> changeListener, Consumer<DropDownElement<T>> customizer) {
    DropDownElement<T> dropDownElement = CustomDropDownElement.of(displayName, iconData, initialValue, values, changeListener);
    dropDownElement.setSettingEnabled(this.settingsEnabledByDefault);
    customizer.accept(dropDownElement);
    return dropDownElement;
  }

  @Nonnull
  @Override
  public ButtonElement brewButtonElement(String displayName, ControlElement.IconData iconData, String inButtonName, Consumer<ButtonElement> clickListener, Consumer<ButtonElement> customizer) {
    ButtonElement element = new ButtonElement(displayName, iconData, inButtonName, clickListener);
    element.setSettingEnabled(this.settingsEnabledByDefault);
    customizer.accept(element);
    return element;
  }

  @Nonnull
  @Override
  public PlayerSkinRenderElement brewRenderElement(Consumer<PlayerSkinRenderElement> customizer) {
    PlayerSkinRenderElement element = new PlayerSkinRenderElement();
    customizer.accept(element);
    return element;
  }

  @Override
  public boolean areSettingsEnabledByDefault() {
    return this.settingsEnabledByDefault;
  }

  @Override
  public void setSettingsEnabledByDefault(boolean settingsEnabledByDefault) {
    this.settingsEnabledByDefault = settingsEnabledByDefault;
  }
}
