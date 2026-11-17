package dev.xcolorful.customgun.client.model.bake;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * 26.2 延迟渲染的静态优化落点：把「常驻显存的烘焙网格」挂进原版 {@code Custom} 组的一次绘制
 * <ul>
 *     <li>几何仍在 {@code submitCustomGeometry} 的回调里提交，顺序、模板序列与光影包的 program 映射都与即时块一致</li>
 *     <li>回调不写顶点，而是往当前 {@code RenderTypeFeatureRenderer.Group} 追加一条「外部绘制」：
 *         顶点数据是烘焙网格自己的 {@link GpuBuffer}，绘制时原版 {@code PreparedRenderType#drawFromBuffer} 照常画</li>
 *     <li>每条外部绘制的 {@code DynamicTransforms} 用「调用方模型视图 × 实例姿态」写入，因此烘焙顶点保持模型空间</li>
 *     <li>拿不到当前组（不在 prepare 阶段、或 mixin 未生效）时 {@link #register} 返回 {@code false}，
 *         调用方退回顶点回放，画面始终正确</li>
 * </ul>
 * 组由 {@code RenderTypeFeatureRenderer$Group} 的 mixin 在构造时激活，{@code prepareGroup} 结束时清空
 */
@ApiStatus.AvailableSince("26.2")
public final class BakedDrawSink {

    private BakedDrawSink() {}

    /**
     * 当前正在 prepare 的 {@code Custom} 组；由组的 mixin 实现
     */
    public interface Group {
        /**
         * 追加一条外部绘制
         * @param pose          实例姿态；绘制矩阵 = 调用方模型视图 × pose（着色器据此变换模型空间顶点）
         * @param bakedPipeline 无光影时用的烘焙管线（把法线算进姿态）；{@code null} 表示用 RenderType 自带的管线
         */
        void cgc$appendBakedDraw(RenderType renderType,
                                 GpuBuffer vertexBuffer,
                                 int indexCount,
                                 PrimitiveTopology topology,
                                 Matrix4f pose,
                                 @Nullable RenderPipeline bakedPipeline);
    }

    /**
     * 顶点数据来自外部 {@link GpuBuffer} 的 {@code StagedVertexBuffer.Draw}
     * <ul>
     *     <li>这类 draw 不参与原版的上传（顶点数为 0，缓冲区间为空）</li>
     *     <li>索引走原版的顺序索引缓冲，口径与非排序绘制的 {@code StagedVertexBuffer} 一致</li>
     * </ul>
     */
    public interface ExternalDraw {
        void cgc$setExternal(GpuBuffer vertexBuffer, int indexCount, PrimitiveTopology topology, Matrix4f pose);

        @Nullable GpuBuffer cgc$vertexBuffer();

        int cgc$indexCount();

        @Nullable PrimitiveTopology cgc$topology();

        @Nullable Matrix4f cgc$pose();
    }

    private static final ThreadLocal<@Nullable Group> ACTIVE = new ThreadLocal<>();

    /**
     * 刚取到执行信息的这次绘制；{@code executeGroup} 拿到后紧接着就发起绘制
     */
    private static final ThreadLocal<@Nullable PendingDraw> PENDING = new ThreadLocal<>();

    private record PendingDraw(Object executeInfo, Matrix4f pose) {}

    public static void activate(Group group) {
        ACTIVE.set(group);
    }

    public static void clear() {
        ACTIVE.remove();
    }

    /**
     * 登记一次外部绘制
     * @param bakedPipeline 无光影时用的烘焙管线；{@code null} 时用 RenderType 自带的管线（开光影包）
     * @return {@code false} 表示当前没有可登记的组，调用方应退回顶点回放
     */
    public static boolean register(RenderType renderType,
                                   GpuBuffer vertexBuffer,
                                   int indexCount,
                                   PrimitiveTopology topology,
                                   Matrix4f pose,
                                   @Nullable RenderPipeline bakedPipeline) {
        @Nullable Group group = ACTIVE.get();
        if (group == null) return false;
        group.cgc$appendBakedDraw(renderType, vertexBuffer, indexCount, topology, pose, bakedPipeline);
        return true;
    }

    public static void setPendingDraw(Object executeInfo, Matrix4f pose) {
        PENDING.set(new PendingDraw(executeInfo, pose));
    }

    /**
     * 取出与本次执行信息配对的实例姿态
     * <ul>
     *     <li>光影包的 {@code iris_NormalMat} 取自绘制时的 {@code RenderSystem} 模型视图栈，
     *         所以绘制期间要再乘一次姿态（1.21.11 的同一处修复）</li>
     *     <li>只认配对的那一次，避免别的执行信息误用残留姿态</li>
     * </ul>
     * @return {@code null} 表示这次绘制不是外部缓冲
     */
    public static @Nullable Matrix4f takePendingPose(Object executeInfo) {
        @Nullable PendingDraw pending = PENDING.get();
        if (pending == null) return null;
        PENDING.remove();
        return pending.executeInfo() == executeInfo ? pending.pose() : null;
    }
}
