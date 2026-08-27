package dev.xcolorful.customgun.neoforgeclient.minecraft.pipeline;

import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
public enum NeoPipelineModifier {
    NO_COLOR_WRITE(PipelineModifier.NO_COLOR_WRITE),
    NO_DEPTH_WRITE(PipelineModifier.NO_DEPTH_WRITE),
    NO_DEPTH_TEST(PipelineModifier.NO_DEPTH_TEST),
    ;

    public final PipelineModifier pipelineModifier;
    public final ResourceKey<net.neoforged.neoforge.client.pipeline.PipelineModifier> registryKey;
    public final net.neoforged.neoforge.client.pipeline.PipelineModifier modifier;
    NeoPipelineModifier(PipelineModifier pipelineModifier) {
        this.pipelineModifier = pipelineModifier;
        this.registryKey = CustomGunNeoforge.mcRegistry.createResourceKey(net.neoforged.neoforge.client.pipeline.PipelineModifier.MODIFIERS_KEY, this.pipelineModifier.getRegistryLocation());
        this.modifier = pipelineModifier.getModifier()::apply;
    }

    public static NeoPipelineModifier of(PipelineModifier pipelineModifier) {
        return switch (pipelineModifier) {
            case NO_COLOR_WRITE -> NO_COLOR_WRITE;
            case NO_DEPTH_WRITE -> NO_DEPTH_WRITE;
            case NO_DEPTH_TEST -> NO_DEPTH_TEST;
        };
    }
}
