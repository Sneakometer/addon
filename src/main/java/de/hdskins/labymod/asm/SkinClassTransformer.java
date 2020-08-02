package de.hdskins.labymod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class SkinClassTransformer implements IClassTransformer {

    public static List<String> s = new ArrayList<>();

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!name.equals("bnp$3") && !name.equals("net.minecraft.client.resources.SkinManager$3")) {
            return basicClass;
        }

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("run") && method.desc.equals("()V")) {
                this.transform(method, name, name.equals("bnp$3"));
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private void transform(MethodNode method, String className, boolean obfuscated) {
        for (int i = 0; i < method.instructions.size(); i++) {
            AbstractInsnNode node = method.instructions.get(i);
            if (node instanceof MethodInsnNode) {
                MethodInsnNode methodNode = (MethodInsnNode) node;
                if (methodNode.owner.equals("java/util/Map") &&
                        methodNode.name.equals("isEmpty") &&
                        methodNode.desc.equals("()Z")) {

                    String profileDesc = "Lcom/mojang/authlib/GameProfile;";

                    InsnList list = new InsnList();

                    list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    list.add(new FieldInsnNode(Opcodes.GETFIELD, className, obfuscated ? "a" : "profile", profileDesc));

                    list.add(new VarInsnNode(Opcodes.ALOAD, 1));

                    list.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HdTextureProvider.class.getName(),
                            "fillProperties",
                            "(" + profileDesc + "Ljava/util/Map;)V",
                            false
                    ));

                    method.instructions.insertBefore(methodNode.getPrevious().getPrevious(), list);
                    break;
                }
            }
        }
    }

}
