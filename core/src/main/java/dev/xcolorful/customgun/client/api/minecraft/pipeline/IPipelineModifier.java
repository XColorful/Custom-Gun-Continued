package dev.xcolorful.customgun.client.api.minecraft.pipeline;

//import com.mojang.blaze3d.pipeline.RenderPipeline; // 1.21.4临时没有RenderPipeline
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * 封装 PipelineModifier
 */
@ApiStatus.AvailableSince("1.21.6")
@FunctionalInterface
public interface IPipelineModifier {

    Object apply(Object pipeline,
                         ResourceLocation pipelineLocation);
}
