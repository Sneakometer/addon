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
import de.hdskins.labymod.shared.asm.achievement.AchievementRenderTransformerV112;
import de.hdskins.labymod.shared.asm.achievement.AchievementRenderTransformerV18;
import de.hdskins.labymod.shared.asm.debug.DebugScreenTransformerV112;
import de.hdskins.labymod.shared.asm.debug.DebugScreenTransformerV18;
import de.hdskins.labymod.shared.asm.draw.DrawUtilsTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

import javax.annotation.Nonnull;
import java.util.Map;

public class MainTransformer implements IClassTransformer {

  private final Map<String, IClassTransformer> transformers = TransformerMapper.mapper()
    .putEntry(new DrawUtilsTransformer(), "net.labymod.utils.DrawUtils")
    // 1.8 achievement render
    .putEntry(new AchievementRenderTransformerV18(), "net.minecraft.client.gui.achievement.GuiAchievement", "ayd")
    // 1.12 achievement render
    .putEntry(new AchievementRenderTransformerV112(), "net.minecraft.client.gui.toasts.GuiToast", "bkc")
    // 1.8 debug screen renderer
    .putEntry(new DebugScreenTransformerV18(), "net.minecraft.client.gui.GuiOverlayDebug", "avv")
    // 1.12 debug screen renderer
    .putEntry(new DebugScreenTransformerV112(), "net.minecraft.client.gui.GuiOverlayDebug")
    .build();

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    IClassTransformer transformer = this.transformers.get(name);
    return transformer == null ? basicClass : transformer.transform(name, transformedName, basicClass);
  }

  private static final class TransformerMapper {

    private final ImmutableMap.Builder<String, IClassTransformer> transformers = ImmutableMap.builder();

    @Nonnull
    public static TransformerMapper mapper() {
      return new TransformerMapper();
    }

    @Nonnull
    public TransformerMapper putEntry(@Nonnull IClassTransformer transformer, @Nonnull String... classes) {
      for (String aClass : classes) {
        this.transformers.put(aClass, transformer);
      }
      return this;
    }

    @Nonnull
    public Map<String, IClassTransformer> build() {
      return this.transformers.build();
    }
  }
}
