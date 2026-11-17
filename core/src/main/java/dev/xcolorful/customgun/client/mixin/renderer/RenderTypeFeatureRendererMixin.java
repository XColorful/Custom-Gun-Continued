package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.model.bake.BakedDrawSink;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2 延迟渲染的静态优化（渲染重构B1-a）：一次 prepare 结束后清掉当前组
 * <ul>
 *     <li>组只在 {@code prepareGroup} 期间存在，外部绘制也只在这次调用里登记</li>
 *     <li>不清空会让提交阶段的回调把几何挂到上一帧的组上（那一组已经执行完，几何静默消失）</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
@Mixin(RenderTypeFeatureRenderer.class)
public class RenderTypeFeatureRendererMixin {

    @Inject(method = "prepareGroup", at = @At("RETURN"))
    private void cgc$clearBakedDrawSink(CallbackInfo ci) {
        BakedDrawSink.clear();
    }
}
