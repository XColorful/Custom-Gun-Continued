package dev.xcolorful.customgun.neoforgeclient.minecraft.stencil;

import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilState;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
public class NeoStencilOperator implements IStencilOperator {

    @Override
    public void applyStencil(StencilState state) {
    }

    @Override
    public void pushPipelineModifier(PipelineModifier modifier) {
    }

    @Override
    public void popPipelineModifier() {
    }
}
