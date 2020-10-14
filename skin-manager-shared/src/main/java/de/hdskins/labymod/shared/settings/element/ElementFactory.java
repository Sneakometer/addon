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
import de.hdskins.labymod.shared.settings.element.elements.PlayerSkinRenderElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public interface ElementFactory {

    static ElementFactory defaultFactory() {
        return DefaultElementFactory.DEFAULT;
    }

    @Nonnull
    StringElement brewStringElement(String displayName, ControlElement.IconData iconData, String currentValue, Consumer<String> changeListener, Consumer<StringElement> customizer);

    @Nonnull
    ChangeableBooleanElement brewBooleanElement(String displayName, ControlElement.IconData iconData, String on, String off, boolean currentValue,
                                                Function<Boolean, CompletableFuture<Boolean>> toggleListener, Consumer<ChangeableBooleanElement> customizer);

    @Nonnull <T> DropDownElement<T> brewDropDownElement(String displayName, ControlElement.IconData iconData, T initialValue, List<T> values,
                                                        Consumer<T> changeListener, Consumer<DropDownElement<T>> customizer);

    @Nonnull
    ButtonElement brewButtonElement(String displayName, ControlElement.IconData iconData, String inButtonName, Runnable clickListener, Consumer<ButtonElement> customizer);

    @Nonnull
    PlayerSkinRenderElement brewRenderElement(Consumer<PlayerSkinRenderElement> customizer);
}
