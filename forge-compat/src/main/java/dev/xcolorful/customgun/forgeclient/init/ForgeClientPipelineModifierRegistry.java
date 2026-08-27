package dev.xcolorful.customgun.forgeclient.init;

import dev.xcolorful.customgun.client.init.ClientPipelineModifierRegistry;
import dev.xcolorful.customgun.forgeclient.minecraft.pipeline.ForgePipelineModifier;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
//@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientPipelineModifierRegistry {

    private static final ClientPipelineModifierRegistry CLIENT_PIPELINE_MODIFIER_REGISTRY = ClientPipelineModifierRegistry.get();

//    @SubscribeEvent
    public static void onRegisterPipelineModifier(Object event) {
        CLIENT_PIPELINE_MODIFIER_REGISTRY.onRegisterPipelineModifiers((modifier) -> {
            ForgePipelineModifier forgePipelineModifier = ForgePipelineModifier.of(modifier);
            var registryKey = forgePipelineModifier.registryKey;
        });
    }
}
