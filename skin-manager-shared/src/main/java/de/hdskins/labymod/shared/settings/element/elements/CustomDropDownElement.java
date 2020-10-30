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
package de.hdskins.labymod.shared.settings.element.elements;

import net.labymod.gui.elements.DropDownMenu;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class CustomDropDownElement<T> extends DropDownElement<T> {

    public CustomDropDownElement(String displayName, ControlElement.IconData iconData, T initialValue, List<T> values, Consumer<T> changeListener) {
        super(displayName, "", buildDropDownMenu(initialValue, values), iconData, null);
        super.setChangeListener(changeListener);
    }

    @Nonnull
    private static <T> DropDownMenu<T> buildDropDownMenu(T initialValue, List<T> values) {
        DropDownMenu<T> dropDownMenu = new DropDownMenu<>("", 0, 0, 0, 0);
        dropDownMenu.setSelected(initialValue);
        for (T value : values) {
            dropDownMenu.addOption(value);
        }
        return dropDownMenu;
    }
}
