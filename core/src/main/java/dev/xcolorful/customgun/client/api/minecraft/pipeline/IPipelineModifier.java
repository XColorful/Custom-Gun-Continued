package dev.xcolorful.customgun.client.api.minecraft.pipeline;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * 封装 PipelineModifier
 */
@ApiStatus.AvailableSince("1.21.6")
@FunctionalInterface
public interface IPipelineModifier {

    RenderPipeline apply(RenderPipeline pipeline,
                         Identifier pipelineLocation);
}
