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
package de.hdskins.labymod.shared.asm;

import com.google.common.collect.ImmutableMap;
import de.hdskins.labymod.shared.asm.draw.DrawUtilsTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Map;

public class MainTransformer implements IClassTransformer {

  private final Map<String, IClassTransformer> transformers = ImmutableMap.of("net.labymod.utils.DrawUtils", new DrawUtilsTransformer());

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    IClassTransformer transformer = this.transformers.get(name);
    return transformer == null ? basicClass : transformer.transform(name, transformedName, basicClass);
  }
}
