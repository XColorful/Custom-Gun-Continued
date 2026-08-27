package dev.xcolorful.customgun.neoforgeclient.minecraft.stencil;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilState;
import dev.xcolorful.customgun.neoforgeclient.minecraft.pipeline.NeoPipelineModifier;
import net.neoforged.neoforge.client.stencil.StencilPerFaceTest;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
public class NeoStencilOperator implements IStencilOperator {

    @Override
    public void applyStencil(StencilState state) {
        StencilPerFaceTest front = new StencilPerFaceTest(
            StencilOperationHelper.convert(state.sFrontFail),
            StencilOperationHelper.convert(state.sFrontDepthFail),
            StencilOperationHelper.convert(state.sFrontPass),
            StencilFunctionHelper.convert(state.sFrontFunc)
        );
        StencilPerFaceTest back = new StencilPerFaceTest(
                StencilOperationHelper.convert(state.sBackFail),
                StencilOperationHelper.convert(state.sBackDepthFail),
                StencilOperationHelper.convert(state.sBackPass),
                StencilFunctionHelper.convert(state.sBackFunc)
        );

        RenderSystem.enableStencil(new StencilTest(
                front,
                back,
                state.sReadMask,
                state.sWriteMask,
                state.sRef
        ));
    }

    @Override
    public void pushPipelineModifier(PipelineModifier modifier) {
        RenderSystem.pushPipelineModifier(NeoPipelineModifier.of(modifier).registryKey);
    }

    @Override
    public void popPipelineModifier() {
        RenderSystem.popPipelineModifier();
    }
}
