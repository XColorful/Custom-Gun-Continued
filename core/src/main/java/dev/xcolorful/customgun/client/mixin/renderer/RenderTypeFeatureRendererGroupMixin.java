package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.model.bake.BakedDrawSink;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * 26.2 延迟渲染的静态优化（渲染重构B1-a）：让烘焙网格以「外部缓冲绘制」的身份进入原版 {@code Custom} 组
 * <ul>
 *     <li>{@code CustomFeatureRenderer} 按提交顺序把几何写进组；这里在同一个组里追加一条 draw，
 *         顶点数据换成烘焙网格常驻显存的 {@link GpuBuffer}，顺序与模板序列保持原版语义</li>
 *     <li>{@code DynamicTransforms} 由 {@code RenderType#prepare()} 从 {@code RenderSystem} 的模型视图栈取，
 *         所以取之前把实例姿态乘进去：绘制矩阵 = 模型视图 × 姿态，烘焙顶点保持模型空间</li>
 *     <li><b>无光影</b>把管线换成烘焙版（{@code cgc_baked_entity}）：原版 {@code entity} 着色器不做法线变换，
 *         模型空间法线会整枪偏暗；法线矩阵走 {@code TextureMat}，见 {@link #cgc$prepareBaked}</li>
 *     <li>不新开 feature type；光影包下仍用 RenderType 原本的管线与 program</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public class RenderTypeFeatureRendererGroupMixin implements BakedDrawSink.Group {

    @Shadow
    private StagedVertexBuffer stagedBuffer;
    @Shadow
    private List<StagedVertexBuffer.Draw> draws;
    @Shadow
    private List<PreparedRenderType> drawRenderTypes;
    @Shadow
    private StagedVertexBuffer.Draw lastDraw;
    @Shadow
    private RenderType lastRenderType;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cgc$activateSink(StagedVertexBuffer stagedBuffer, boolean canReorder, CallbackInfo ci) {
        BakedDrawSink.activate(this);
    }

    @Override
    public void cgc$appendBakedDraw(RenderType renderType,
                                    GpuBuffer vertexBuffer,
                                    int indexCount,
                                    PrimitiveTopology topology,
                                    Matrix4f pose,
                                    @Nullable RenderPipeline bakedPipeline) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(pose);
        PreparedRenderType prepared;
        try {
            PreparedRenderType base = renderType.prepare();
            prepared = bakedPipeline == null ? base : cgc$prepareBaked(renderType, bakedPipeline, base, pose);
        } finally {
            modelViewStack.popMatrix();
        }

        // 不经过 Group#getOrAddDraw：那是给顶点流用的，会按 RenderType 合并到已有 draw 里，
        // 外部缓冲是独立的一份，必须各占一条
        StagedVertexBuffer.Draw draw = this.stagedBuffer.appendDraw(
                renderType.format(), renderType.primitiveTopology(), null);
        ((BakedDrawSink.ExternalDraw) (Object) draw).cgc$setExternal(
                vertexBuffer, indexCount, topology, new Matrix4f(pose));
        this.draws.add(draw);
        this.drawRenderTypes.add(prepared);

        // 顺序索引缓冲在 prepare 阶段就备好：绘制阶段再扩容会在 execute 期间重建缓冲
        RenderSystem.getSequentialBuffer(topology).getBuffer(indexCount);

        // 原版的合并逻辑以 lastDraw 为准；不重置的话，之后的即时几何会被并进外部绘制之前的那条 draw，
        // 顺序相对外部绘制前移
        this.lastDraw = null;
        this.lastRenderType = null;
    }

    /**
     * 把 {@code prepare()} 的结果换成烘焙管线，并补上实例姿态
     * <ul>
     *     <li>位置：{@code ModelViewMat} = 模型视图 × 姿态（此时姿态已压进模型视图栈），layering 修饰器照原版再作用一次</li>
     *     <li>{@code TextureMat} 放<b>姿态本身</b>，烘焙网格不使用纹理变换（glint 之类不进烘焙路径）：
     *         原版 {@code entity} 着色器拿顶点坐标（即时块已由 CPU 乘过姿态）算雾、拿顶点里的 {@code Normal}
     *         （即时块是 CPU 写的 {@code IT(姿态)}）点乘世界空间光照方向，两者都要这一步姿态</li>
     * </ul>
     */
    private static PreparedRenderType cgc$prepareBaked(RenderType renderType,
                                                       RenderPipeline bakedPipeline,
                                                       PreparedRenderType base,
                                                       Matrix4f pose) {
        Matrix4f modelView = RenderSystem.getModelViewMatrixCopy();
        @Nullable Consumer<Matrix4f> layering = CustomGunClient.getAccessTransformer().getRenderSetupLayering(renderType);
        if (layering != null) layering.accept(modelView);

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(modelView, new Matrix4f(pose));
        return new PreparedRenderType(base.name(), bakedPipeline, base.oitPipelineSet(), dynamicTransforms,
                base.scissorState(), base.textures());
    }
}
