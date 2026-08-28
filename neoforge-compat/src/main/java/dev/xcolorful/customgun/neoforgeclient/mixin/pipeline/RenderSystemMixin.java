package dev.xcolorful.customgun.neoforgeclient.mixin.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.neoforgeclient.minecraft.stencil.NeoStencilOperator;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
为了跨版本提前知道1.21.10neoforge的移植方式，添加此类作为占位符
 */
/**
 * <ul>
 *     1.21.10 起
 *     <li>{@code RenderSystem.enableStencil}/{@code disableStencil} 被移除</li>
 *     <li>模板测试只能通过{@link RenderPipeline} 携带</li>
 *     <li>这里在管线修改器应用之后，把 {@link NeoStencilOperator} 累积的模板测试注入到实际使用的管线中</li>
 * </ul>
 */
@ApiStatus.AvailableSince("1.21.10")
//@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    private static void cgc$applyDynamicStencil(RenderPipeline pipeline,
                                                CallbackInfoReturnable<RenderPipeline> cir) {
        cir.setReturnValue(NeoStencilOperator.applyStencilToPipeline(cir.getReturnValue()));
    }
}
