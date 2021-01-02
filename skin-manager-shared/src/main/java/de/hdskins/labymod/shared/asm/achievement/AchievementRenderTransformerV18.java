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
package de.hdskins.labymod.shared.asm.achievement;

import de.hdskins.labymod.shared.utils.LabyModUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AchievementRenderTransformerV18 implements IClassTransformer {

  private static final String LABYMOD_UTILS = LabyModUtils.class.getName().replace('.', '/');

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(basicClass);
    classReader.accept(classNode, 0);

    for (MethodNode method : classNode.methods) {
      if (!method.desc.equals("()V")) {
        continue;
      }

      if (method.name.equals("drawToast") || method.name.equals("func_191783_a") || method.name.equals("a")) {
        AbstractInsnNode node = method.instructions.getFirst();
        method.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, LABYMOD_UTILS, "updateAchievementWindow", "()V", false));
      }
    }

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }

}
