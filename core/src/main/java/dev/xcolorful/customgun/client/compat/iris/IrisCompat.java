package dev.xcolorful.customgun.client.compat.iris;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

/**
 * <ul>
 *     <li>1.20.1为Oculus</li>
 *     <li>1.21.1+为Iris Shaders</li>
 * </ul>
 */
public class IrisCompat {

    public static boolean isRenderShadow() {
        // mixin注入点
        return false;
    }

    public static boolean isUsingRenderPack() {
        // mixin注入点
        return false;
    }

    /**
     * <ul>
     *     <li>level 渲染期间 {@code RenderBuffers.bufferSource()} 给的是全缓冲源</li>
     *     <li>它把 {@code endBatch(RenderType)} 实现成了空方法，per-type 立即提交会静默失效</li>
     *     <li>只有无参 {@code endBatch()} 会真正提交并清空已排队几何体</li>
     * </ul>
     * 1.21.10 移除
     * <ul>
     *     <li>Iris 1.9.7 起整个 {@code batchedentityrendering} 模块被删掉</li>
     *     <li>{@code RenderBuffers.bufferSource()} 不再被换成全缓冲源</li>
     *     <li>{@code endBatch(RenderType)} 就是真的提交，CGC 那句兜底不需要接管</li>
     * </ul>
     * @return 是否接管渲染
     */
//    @Deprecated(since = "1.21.10")
    public static boolean endBatch(MultiBufferSource.BufferSource bufferSource) {
        // mixin注入点
        return false;
    }

    /**
     * 手部 poseStack 里采到的位移是否已经是「世界轴」量
     * <ul>
     *     <li>vanilla 的 {@code GameRenderer.renderItemInHand} 把手部 poseStack 的基底设成 {@code camera.rotation()}（视图空间→世界），采到的是世界轴量</li>
     *     <li>Iris 的 {@code HandRenderer} 交给 {@code renderHandsWithItems} 的是空 PoseStack，基底是单位阵，采到的是视图空间量，用之前得乘 {@code camera.rotation()} 换算</li>
     * </ul>
     * @deprecated 目前不再使用
     * @return false 表示需要换算
     */
    @Deprecated
    @ApiStatus.AvailableSince("1.21.1")
    public static boolean isHandPoseStackWorldSpace() {
        // mixin注入点
        return true;
    }

    @ApiStatus.AvailableSince("1.21.6")
    public static void registerRenderPipelines(Consumer<RenderPipeline> registrar) {
        // mixin注入点
    }
}
