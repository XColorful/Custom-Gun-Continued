/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.api.minecraft.pipeline.IPipelineModifier;
import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.21.6")
public class ClientPipelineModifierRegistry {

    private static final ClientPipelineModifierRegistry INSTANCE = new ClientPipelineModifierRegistry();
    public static ClientPipelineModifierRegistry get() {
        return INSTANCE;
    }
    private ClientPipelineModifierRegistry() {}

    @FunctionalInterface
    public interface PipelineModifierRegister {
        void register(ResourceLocation registryLocation,
                      IPipelineModifier modifier);
    }
    public void onRegisterPipelineModifiers(PipelineModifierRegister register) {
        for (PipelineModifier pipelineModifier : PipelineModifier.values() ) {
            register.register(pipelineModifier.getRegistryLocation(), pipelineModifier.getModifier());
        }
    }
}
