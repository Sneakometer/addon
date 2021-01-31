package de.hdskins.labymod.shared.asm.tab;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TabOverlayGuiTransformer implements IClassTransformer {

  private static final String RENDERER_NAME = "de/hdskins/labymod/shared/gui/TabRenderer";

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(basicClass);
    classReader.accept(classNode, 0);

    for (MethodNode method : classNode.methods) {
      if (method.name.equals("newTabOverlay")) {
        AbstractInsnNode last = this.findReturn(method);
        if (last == null) {
          continue;
        }

        // invoke TabRenderer.renderTabOverlay after the one by LabyMod has been rendered
        InsnList list = new InsnList();

        // calculate center: width / 2
        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // load width
        list.add(new InsnNode(Opcodes.ICONST_2)); // load 2
        list.add(new InsnNode(Opcodes.IDIV)); // width / 2
        // calculate xOffset: var16 / 2
        list.add(new VarInsnNode(Opcodes.ILOAD, 20)); // load var16
        list.add(new InsnNode(Opcodes.ICONST_2)); // load 2
        list.add(new InsnNode(Opcodes.IDIV)); // var16 / 2
        // invoke renderTabOverlay
        list.add(this.invokeRenderMethod());

        method.instructions.insertBefore(last, list);
      }
    }

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }

  @Nonnull
  private MethodInsnNode invokeRenderMethod() {
    return new MethodInsnNode(
      Opcodes.INVOKESTATIC,
      RENDERER_NAME,
      "renderTabOverlay",
      "(II)V",
      false
    );
  }

  @Nullable
  private AbstractInsnNode findReturn(MethodNode method) {
    AbstractInsnNode node = method.instructions.getLast();

    do {
      node = node.getPrevious();
    } while (node != null && node.getOpcode() != Opcodes.RETURN);

    return node;
  }
}
