package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.client.init.ClientPipelineModifierRegistry;
import dev.xcolorful.customgun.neoforgeclient.minecraft.pipeline.NeoPipelineModifier;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
//@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientPipelineModifierRegistry {

    private static final ClientPipelineModifierRegistry CLIENT_PIPELINE_MODIFIER_REGISTRY = ClientPipelineModifierRegistry.get();

//    @SubscribeEvent
    public static void onRegisterPipelineModifier(Object event) {
        CLIENT_PIPELINE_MODIFIER_REGISTRY.onRegisterPipelineModifiers((modifier) -> {
            NeoPipelineModifier neoPipelineModifier = NeoPipelineModifier.of(modifier);
            var registryKey = neoPipelineModifier.registryKey;
        });
    }
}
