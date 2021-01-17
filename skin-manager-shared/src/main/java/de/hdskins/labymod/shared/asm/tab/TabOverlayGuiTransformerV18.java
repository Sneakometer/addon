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

public class TabOverlayGuiTransformerV18 implements IClassTransformer {

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

        //list.add(new InsnNode(Opcodes.IREM)); // width / 2 - var16 / 2

        list.add(this.invokeRenderMethod()); // invoke renderTabOverlay

        method.instructions.insertBefore(last, list);

      } else if (method.name.equals("oldTabOverlay")) {
        AbstractInsnNode last = this.findInvokeVirtual(method, "drawRightString");
        if (last == null) {
          continue;
        }

        // invoke TabRenderer.renderTabOverlay after the one by LabyMod has been rendered
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ILOAD, 19)); // load center

        // calculate xOffset: (var46 / 2) * (var17 / 2)
        list.add(new VarInsnNode(Opcodes.ILOAD, 18)); // load var46
        list.add(new InsnNode(Opcodes.ICONST_2)); // load 2
        list.add(new InsnNode(Opcodes.IDIV)); // var17 / 2
        list.add(new VarInsnNode(Opcodes.ILOAD, 13)); // load var13
        list.add(new InsnNode(Opcodes.ICONST_2)); // load 2
        list.add(new InsnNode(Opcodes.IDIV)); // var17 / 2
        list.add(new InsnNode(Opcodes.IMUL)); // var46 * var17

        list.add(this.invokeRenderMethod()); // invoke renderTabOverlay

        method.instructions.insert(last, list);
      }
    }

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }

  private MethodInsnNode invokeRenderMethod() {
    return new MethodInsnNode(
      Opcodes.INVOKESTATIC,
      RENDERER_NAME,
      "renderTabOverlay",
      "(II)V",
      false
    );
  }

  private AbstractInsnNode findReturn(MethodNode method) {
    AbstractInsnNode node = method.instructions.getLast();

    do {
      node = node.getPrevious();
    } while (node != null && node.getOpcode() != Opcodes.RETURN);

    return node;
  }

  private AbstractInsnNode findInvokeVirtual(MethodNode method, String methodName) {
    AbstractInsnNode node = method.instructions.getLast();

    do {
      node = node.getPrevious();
    } while (node != null && (node.getOpcode() != Opcodes.INVOKEVIRTUAL || (node instanceof MethodInsnNode && !((MethodInsnNode) node).name.equals(methodName))));

    return node;
  }

}
