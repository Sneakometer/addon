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
