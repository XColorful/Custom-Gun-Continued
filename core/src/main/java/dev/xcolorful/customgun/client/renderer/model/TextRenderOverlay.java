package dev.xcolorful.customgun.client.renderer.model;

import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 模型文字的最终覆盖层
 * <ul>
 *     文字不再走 {@code submitText}：
 *     <li>提交进手部 pass 的文字会被 Iris 派到光影包的手部程序，而文字顶点格式没有 Normal，光照语义对不上，表现为发黑与拖影</li>
 *     <li>改为在手部几何的 flush 阶段排队（{@link #queue}），等这一遍真的画完之后，用快照的手部投影与模型视图配合 {@link ClientRenderRegistry.ModelTextRenderState} 自己的管线重画（{@link #flush}）</li>
 *     <li>两个刷新点都调用 {@link #flush}：level 渲染之后（光影下 Iris 已把手部搬进{@code LevelRenderer#render}）与 {@code renderLevel} 返回前（无光影时手部在之后才画）</li>
 *     <li>队列为空时开销就是一次判空</li>
 *     <li>深度仍按主深度缓冲生效：目镜黑盘挡在文字前面，开镜孔里才可见</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
public final class TextRenderOverlay {

    private TextRenderOverlay() {
    }

    /**
     * 把一条文字排进覆盖层
     * <ul>
     *     <li>调用点在模型几何的 flush 回调里</li>
     *     <li>此刻 {@code RenderSystem} 里挂的就是手部那一遍的矩阵</li>
     * </ul>
     */
    public static void queue(Matrix4f pose,
                             Matrix3f normal,
                             float x, float y,
                             String text,
                             boolean shadow,
                             int packedLight,
                             int color) {
    }

    /**
     * 把排队中的文字画到最终画面上
     */
    public static void flush() {
    }

    private record QueuedText(Matrix4f pose,
                              Matrix3f normal,
                              float x, float y,
                              String text,
                              boolean shadow,
                              int packedLight,
                              int color) {
    }
}
