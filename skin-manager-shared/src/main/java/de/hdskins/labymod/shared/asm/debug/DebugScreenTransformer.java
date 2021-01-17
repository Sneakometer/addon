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
package de.hdskins.labymod.shared.asm.debug;

import com.google.common.collect.ImmutableSet;
import net.labymod.main.Source;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ListIterator;
import java.util.Set;

public class DebugScreenTransformer implements IClassTransformer {

  private static final boolean IS_V18 = Source.ABOUT_MC_VERSION.startsWith("1.8");
  private static final String DEBUG_SCREEN_UTILS = DebugScreenUtils.class.getName().replace('.', '/');

  private static final Set<String> V1_8_CLASSES = ImmutableSet.of("avv", "net.minecraft.client.gui.GuiOverlayDebug");
  private static final Set<String> V1_12_CLASSES = ImmutableSet.of("bjd", "net.minecraft.client.gui.GuiOverlayDebug");

  private static final Set<String> METHODS = ImmutableSet.of("renderDebugInfoLeft()V", "a()V");

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    if (IS_V18 && V1_8_CLASSES.contains(name)) {
      return this.doTransform(basicClass);
    } else if (!IS_V18 && V1_12_CLASSES.contains(name)) {
      return this.doTransform(basicClass);
    }
    return basicClass;
  }

  private byte[] doTransform(byte[] basicClass) {
    final ClassNode classNode = new ClassNode();
    final ClassReader classReader = new ClassReader(basicClass);
    classReader.accept(classNode, 0);

    for (MethodNode method : classNode.methods) {
      final String assembled = method.name + method.desc;
      if (METHODS.contains(assembled)) {
        final AbstractInsnNode next = this.findNextNode(method);
        if (next != null) {
          final InsnList list = new InsnList();
          list.add(new VarInsnNode(Opcodes.ALOAD, 1));
          list.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            DEBUG_SCREEN_UTILS,
            "appendScreenInfo",
            "(Ljava/util/List;)V",
            false
          ));
          method.instructions.insertBefore(next, list);
        }
        break;
      }
    }

    final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(writer);
    return writer.toByteArray();
  }

  @Nullable
  private AbstractInsnNode findNextNode(@Nonnull MethodNode methodNode) {
    final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
    while (iterator.hasNext()) {
      final AbstractInsnNode node = iterator.next();
      if (node instanceof MethodInsnNode) {
        final MethodInsnNode methodInsnNode = (MethodInsnNode) node;
        if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
          return iterator.hasNext() ? iterator.next().getNext() : null;
        }
      }
    }
    return null;
  }
}
