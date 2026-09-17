package dev.xcolorful.customgun.neoforgeclient.mixin.pipeline;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.xcolorful.customgun.neoforgeclient.minecraft.stencil.NeoStencilOperator;
import net.neoforged.neoforge.client.pipeline.PipelineModifierStack;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
为了跨版本提前知道26.3neoforge的移植方式，添加此类作为占位符
 */
/**
 * <ul>
 *     26.3
 *     <li>{@code RenderSystem#applyPipelineModifiers} 被移除</li>
 *     <li>修饰器栈 {@code PipelineModifierStack} 只由 {@code RenderSystem#getCompiledPipelineNullable} 与 {@code RenderTypePipelineBake} 调用</li>
 *     <li>两者都会经过 {@code PipelineModifierStack#apply}，因此改为在它之上注入，覆盖范围与 26.2 一致</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.3")
@Mixin(PipelineModifierStack.class)
public class PipelineModifierStackMixin {

    @Inject(method = "apply",
            at = @At("RETURN"),
            cancellable = true)
    private void cgc$applyDynamicStencil(RenderPipeline pipeline,
                                         CallbackInfoReturnable<RenderPipeline> cir) {
        cir.setReturnValue(NeoStencilOperator.applyStencilToPipeline(cir.getReturnValue()));
    }
}
