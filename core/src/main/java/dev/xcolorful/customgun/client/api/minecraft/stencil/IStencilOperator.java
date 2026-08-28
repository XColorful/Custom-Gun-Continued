package dev.xcolorful.customgun.client.api.minecraft.stencil;

import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.21.6")
public interface IStencilOperator {

    void applyStencil(StencilState state);

    void pushPipelineModifier(PipelineModifier modifier);

    void popPipelineModifier();

    @ApiStatus.AvailableSince("1.21.10")
    void disableStencil();
}
