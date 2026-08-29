package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.model._AttachmentModelRender;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
为了跨版本提前知道26.2neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("26.2")
//@Mixin(targets = "net.minecraft.client.renderer.feature.CustomFeatureRenderer$Submit")
public class CustomFeatureRendererSubmitMixin {

    /**
     * <ul>
     *     26.2
     *     <li>{@code SimpleFeatureRenderPhase} 会按 {@code BatchableSubmit#batchKey()} 把提交分组</li>
     *     <li>{@code CustomFeatureRenderer.Submit} 的 batchKey 返回 RenderType，而 RenderType 未重写 equals/hashCode</li>
     *     <li>于是所有自定义几何体被塞进一个 HashMap（按身份哈希排序），在 drain 时以 HashMap 迭代顺序输出，打乱了 submit 顺序</li>
     *     <li>枪械目镜的模板算法{@link _AttachmentModelRender}（清空 → 写目镜值 → 镜身 → 遮罩/准心）依赖严格顺序，顺序被打乱后逐帧不稳定，表现为高频闪烁与错误遮罩</li>
     *     <li>这里让自定义几何体不参与 batch 分组，改走按提交顺序保留的 unbatched 列表</li>
     * </ul>
     */
    private void cgc$preserveSubmitOrder(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(null);
    }
}
