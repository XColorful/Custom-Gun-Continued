package dev.xcolorful.customgun.forgeclient.minecraft.pipeline;

import dev.xcolorful.customgun.client.api.minecraft.pipeline.IPipelineModifier;
import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import dev.xcolorful.customgun.forge.CustomGunForge;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
public enum ForgePipelineModifier {
    NO_COLOR_WRITE(PipelineModifier.NO_COLOR_WRITE),
    NO_DEPTH_WRITE(PipelineModifier.NO_DEPTH_WRITE),
    NO_DEPTH_TEST(PipelineModifier.NO_DEPTH_TEST),
    ;

    public final PipelineModifier pipelineModifier;
    public final ResourceKey<IPipelineModifier> registryKey;
    ForgePipelineModifier(PipelineModifier pipelineModifier) {
        this.pipelineModifier = pipelineModifier;
        this.registryKey = CustomGunForge.mcRegistry.createResourceKey(null, this.pipelineModifier.getRegistryLocation());
    }

    public static ForgePipelineModifier of(PipelineModifier pipelineModifier) {
        return switch (pipelineModifier) {
            case NO_COLOR_WRITE -> NO_COLOR_WRITE;
            case NO_DEPTH_WRITE -> NO_DEPTH_WRITE;
            case NO_DEPTH_TEST -> NO_DEPTH_TEST;
        };
    }
}
