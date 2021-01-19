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
package de.hdskins.labymod.shared.asm.labyconnect;

import de.hdskins.labymod.shared.utils.UnbanRequestUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LabyConnectPacketHandlerTransformer implements IClassTransformer {

  private static final String UNBAN_REQUEST_UTILS = UnbanRequestUtils.class.getName().replace('.', '/');

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(basicClass);
    classReader.accept(classNode, 0);

    for (MethodNode method : classNode.methods) {
      final String fullName = method.name + method.desc;
      if (fullName.equals("handle(Lnet/labymod/labyconnect/packets/PacketPlayFriendRemove;)V")) {
        final InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(
          Opcodes.INVOKESTATIC,
          UNBAN_REQUEST_UTILS,
          "handleFriendRemove",
          "(Lnet/labymod/labyconnect/packets/PacketPlayFriendRemove;)V",
          false
        ));
        method.instructions.insertBefore(method.instructions.getFirst(), instructions);
        break;
      }
    }

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }
}
