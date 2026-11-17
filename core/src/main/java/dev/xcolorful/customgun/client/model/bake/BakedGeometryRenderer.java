package dev.xcolorful.customgun.client.model.bake;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * 烘焙网格的提交回调（26.2 延迟渲染）
 * <ul>
 *     <li>优先让烘焙网格的 {@link GpuBuffer} 由原版 {@code Custom} 组的绘制阶段直接画一次，不写顶点（{@link BakedDrawSink}）</li>
 *     <li><b>无光影</b>换成烘焙管线：原版 {@code entity} 着色器直接点乘顶点里的 {@code Normal}，
 *         而烘焙网格是模型空间法线（关光影时整枪偏暗、转动不变化就是这个原因）</li>
 *     <li><b>开光影包</b>仍用 RenderType 自带的管线：光影包的 program 按 {@code iris_NormalMat = IT(模型视图)}
 *         处理法线，换管线反而拿不到 program</li>
 *     <li>拿不到组时退回顶点回放：与 2026-11-16 验收过的路径完全一致，画面不受影响</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
final class BakedGeometryRenderer implements SubmitNodeCollector.CustomGeometryRenderer {

    private final BakedMesh mesh;
    private final RenderType renderType;

    BakedGeometryRenderer(BakedMesh mesh, RenderType renderType) {
        this.mesh = mesh;
        this.renderType = renderType;
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer buffer) {
        @Nullable GpuBuffer vertexBuffer = StaticBake.GPU_RESIDENT_26_2 ? this.mesh.gpuBuffer() : null;
        if (vertexBuffer == null || this.renderType.sortOnUpload()) {
            // 上传排序的 RenderType 需要按帧重排顶点，外部缓冲无法参与；没有缓冲说明网格已被关闭
            cgc$replay(pose, buffer);
            return;
        }

        @Nullable RenderPipeline bakedPipeline = null;
        if (!IrisCompat.isUsingRenderPack()) {
            bakedPipeline = ClientRenderRegistry.BakedEntityRenderState.bakedPipelineFor(this.renderType);
            if (bakedPipeline == null) {
                cgc$replay(pose, buffer);
                return;
            }
        }

        if (!BakedDrawSink.register(this.renderType,
                vertexBuffer, this.mesh.indexCount(), this.mesh.topology(),
                pose.pose(), bakedPipeline)) {
            cgc$replay(pose, buffer);
            return;
        }
        ClientRenderProfiler.countGpuDraw();
    }

    private void cgc$replay(PoseStack.Pose pose, VertexConsumer buffer) {
        this.mesh.replay(pose, buffer);
        ClientRenderProfiler.countReplayDraw();
    }
}
