package dev.xcolorful.customgun.client.compat.iris;

//import com.mojang.blaze3d.pipeline.RenderPipeline;//1.21.4没有RenderPipeline
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
     * @return 是否接管渲染
     */
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
    public static void registerRenderPipelines(Consumer<Object> registrar) {
        // mixin注入点
    }
}
