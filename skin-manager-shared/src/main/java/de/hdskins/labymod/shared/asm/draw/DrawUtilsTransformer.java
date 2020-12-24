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
package de.hdskins.labymod.shared.asm.draw;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DrawUtilsTransformer implements IClassTransformer {

  private static final String SKULL_RENDERER = SkullRenderer.class.getName().replace(".", "/");

  private static void transformRenderSkull(MethodNode methodNode) {
    InsnList insnList = new InsnList();
    insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
    insnList.add(new MethodInsnNode(
      Opcodes.INVOKESTATIC,
      SKULL_RENDERER,
      "renderSkull",
      "(Lcom/mojang/authlib/GameProfile;)V",
      false
    ));
    finish(methodNode, insnList);
  }

  @Nonnull
  private static InsnList transformDrawPlayerHeadBasic() {
    InsnList insnList = new InsnList();
    insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
    insnList.add(new VarInsnNode(Opcodes.ILOAD, 2));
    insnList.add(new VarInsnNode(Opcodes.ILOAD, 3));
    insnList.add(new VarInsnNode(Opcodes.ILOAD, 4));
    return insnList;
  }

  private static void transformDrawPlayerHeadByGameProfile(MethodNode methodNode, InsnList insnList) {
    insnList.add(new MethodInsnNode(
      Opcodes.INVOKESTATIC,
      SKULL_RENDERER,
      "drawPlayerHead",
      "(Lcom/mojang/authlib/GameProfile;III)V",
      false
    ));
    finish(methodNode, insnList);
  }

  private static void transformDrawPlayerHeadByUniqueId(MethodNode methodNode, InsnList insnList) {
    insnList.add(new MethodInsnNode(
      Opcodes.INVOKESTATIC,
      SKULL_RENDERER,
      "drawPlayerHead",
      "(Ljava/util/UUID;III)V",
      false
    ));
    finish(methodNode, insnList);
  }

  private static void transformDrawPlayerHeadByUserName(MethodNode methodNode, InsnList insnList) {
    insnList.add(new MethodInsnNode(
      Opcodes.INVOKESTATIC,
      SKULL_RENDERER,
      "drawPlayerHead",
      "(Ljava/lang/String;III)V",
      false
    ));
    finish(methodNode, insnList);
  }

  private static void finish(MethodNode methodNode, InsnList insnList) {
    // copy the last two statements from the method (return and closing label)
    insnList.add(methodNode.instructions.getLast().getPrevious());
    insnList.add(methodNode.instructions.getLast());
    // override the full method
    methodNode.instructions.clear();
    methodNode.instructions.add(insnList);
  }

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(basicClass);
    classReader.accept(classNode, 0);

    for (MethodNode method : classNode.methods) {
      if (method.name.equals("renderSkull")) {
        transformRenderSkull(method);
      } else if (method.name.equals("drawPlayerHead")) {
        switch (method.desc) {
          case "(Lcom/mojang/authlib/GameProfile;III)V":
            transformDrawPlayerHeadByGameProfile(method, transformDrawPlayerHeadBasic());
            break;
          case "(Ljava/util/UUID;III)V":
            transformDrawPlayerHeadByUniqueId(method, transformDrawPlayerHeadBasic());
            break;
          case "(Ljava/lang/String;III)V":
            transformDrawPlayerHeadByUserName(method, transformDrawPlayerHeadBasic());
            break;
          default:
            break;
        }
      }
    }

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }
}
