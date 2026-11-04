package net.hexnowloading.hexfortress.mixin;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the 150 weight limit of Codec.intRange(1, 150) in StructureTemplatePool.DIRECT_CODEC.
 * <p>
 * In 1.20.1 the constant still lives in the DIRECT_CODEC record lambda. The annotation processor cannot see
 * synthetic lambdas, so no refmap entry is generated; both the official (dev) and SRG (production) names are
 * listed explicitly. Exactly one of them exists in any given environment, hence require = 1.
 */
@Mixin(StructureTemplatePool.class)
public class TemplatePoolWeightMixin {
    @ModifyConstant(
            method = {
                    "lambda$static$1(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;",
                    "m_254834_(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;"
            },
            constant = @Constant(intValue = 150),
            remap = false,
            require = 1
    )
    private static int hexfortress_increaseWeightLimit(int constant) {
        return 5000;
    }
}
