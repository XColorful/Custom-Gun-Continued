/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
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
        void register(PipelineModifier modifier);
    }
    public void onRegisterPipelineModifiers(PipelineModifierRegister register) {
        for (PipelineModifier pipelineModifier : PipelineModifier.values() ) {
            register.register(pipelineModifier);
        }
    }
}
