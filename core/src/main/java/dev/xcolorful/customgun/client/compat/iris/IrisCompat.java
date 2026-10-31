package dev.xcolorful.customgun.client.compat.iris;

import com.mojang.blaze3d.pipeline.RenderPipeline;
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
    public static boolean endBatch(Object bufferSource) {
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

    /**
     * 当前是否处于 Iris 手部通道的「半透明」那一遍
     * <ul>
     *     <li>光影开启时 Iris 把手部渲染搬进 {@code LevelRenderer}，一帧跑两遍：
     *     {@code HandRenderer#renderSolid} 与 {@code HandRenderer#renderTranslucent}</li>
     *     <li>两遍都会调 {@code ItemInHandRenderer#submitHandsWithItems}，于是 RenderHandEvent 每帧派发两次</li>
     *     <li>vanilla 的物品由 Iris 在 {@code submitArmWithItem} 头部按「物品是否半透明」过滤掉其中一遍</li>
     *     <li>CGC 取消 RenderHandEvent 会跳过 {@code submitArmWithItem}，也就绕过了那道过滤：
     *     枪与手臂在两遍里各画一次，第二遍落到 hand_water 程序上，两遍深度相同时互相覆盖
     *     （表现为手臂外层与枪械反射闪烁）</li>
     * </ul>
     * CGC 的动画物品（枪械/配件/弹药）都不是 {@code BlockItem}，Iris 一律按「不透明」处理，
     * 所以半透明那一遍直接不渲染即可
     * <ul>
     *     <li>[1.20.1, 26.2) 无此问题：Iris 尚未把手部渲染搬进 LevelRenderer，手部只画一遍</li>
     * </ul>
     * @return 未安装光影兼容模组 / 未开光影 / 不在 Iris 手部通道时返回 false
     */
    @ApiStatus.AvailableSince("26.2")
    public static boolean isTranslucentHandPass() {
        // mixin注入点
        return false;
    }
}
