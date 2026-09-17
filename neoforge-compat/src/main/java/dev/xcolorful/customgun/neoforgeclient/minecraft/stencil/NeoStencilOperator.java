package dev.xcolorful.customgun.neoforgeclient.minecraft.stencil;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilState;
import dev.xcolorful.customgun.neoforgeclient.minecraft.pipeline.NeoPipelineModifier;
import net.neoforged.neoforge.client.stencil.StencilPerFaceTest;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
public class NeoStencilOperator implements IStencilOperator {

    @Override
    public void applyStencil(StencilState state) {
        StencilPerFaceTest front = new StencilPerFaceTest(
            StencilOperationHelper.convert(state.sFrontFail),
            StencilOperationHelper.convert(state.sFrontDepthFail),
            StencilOperationHelper.convert(state.sFrontPass),
            StencilFunctionHelper.convert(state.sFrontFunc)
        );
        StencilPerFaceTest back = new StencilPerFaceTest(
                StencilOperationHelper.convert(state.sBackFail),
                StencilOperationHelper.convert(state.sBackDepthFail),
                StencilOperationHelper.convert(state.sBackPass),
                StencilFunctionHelper.convert(state.sBackFunc)
        );

        CURRENT_STENCIL.set(new StencilTest(
                front,
                back,
                state.sReadMask,
                state.sWriteMask,
                state.sRef
        ));
    }

    @Override
    public void pushPipelineModifier(PipelineModifier modifier) {
        RenderSystem.pushPipelineModifier(NeoPipelineModifier.of(modifier).registryKey);
    }

    @Override
    public void popPipelineModifier() {
        RenderSystem.popPipelineModifier();
    }

    // ----------1.21.10----------

    /**
     * 1.21.10 移除了 RenderSystem 上的 enableStencil/disableStencil，模板测试改由 {@link RenderPipeline} 携带。
     * 这里把当前要生效的模板测试累积到 ThreadLocal，再由 mixin 在管线应用时注入。
     */
    @ApiStatus.AvailableSince("1.21.10")
    private static final ThreadLocal<StencilTest> CURRENT_STENCIL = new ThreadLocal<>();

    /**
     * 缓存“基础管线 + 模板测试 -> 注入模板后的管线”，保证同一个 {@link RenderPipeline} 对象被复用，
     * 否则 {@code GlDevice#pipelineCache}（IdentityHashMap）每次都会重新编译管线。
     */
    private static final Map<RenderPipeline, Map<StencilTest, RenderPipeline>> STENCIL_PIPELINE_CACHE = new IdentityHashMap<>();

    @ApiStatus.AvailableSince("1.21.10")
    @Override
    public void disableStencil() {
        CURRENT_STENCIL.remove();
    }

    /**
     * 若当前有模板测试，则把其注入管线并返回；否则原样返回
     */
    @ApiStatus.AvailableSince("1.21.10")
    @ApiStatus.Internal
    public static RenderPipeline applyStencilToPipeline(RenderPipeline pipeline) {
        StencilTest stencil = CURRENT_STENCIL.get();
        if (stencil == null) return pipeline;
//        if (pipeline.getStencilTest().filter(stencil::equals).isPresent()) return pipeline; // 26.3移除

        // 26.3 起模板测试不再是 RenderPipeline 上的独立字段，而是 DepthStencilState 的一个分量
        DepthStencilState existing = pipeline.getDepthStencilState();
        if (existing == null) {
            /*
            管线没有深度状态时模板无处安放
            26.2 之前模板是 RenderPipeline 的独立字段，所以“整块移除深度状态”的修饰器（{@link PipelineModifier#NO_DEPTH_TEST}）不会连带抹掉模板；
            26.3 会

            renderSight 的准心正好走这条路：_disableDepthTest() -> NO_DEPTH_TEST（移除深度状态）-> 再画准心
            结果准心不再被模板遮罩，红点在 ocular 之外整片可见
            这里补一个等价的「深度测试恒过、不写深度」（即原来的“关闭深度测试”）只为把模板挂上去
            注意不能改 NO_DEPTH_TEST 本身：GUI（AttachmentCategorySlot 等）也用它，而 GUI 渲染通道
            没有深度附件，带深度状态的管线会被 FrontendRenderPass 直接抛 IllegalStateException
             */
            existing = new DepthStencilState(CompareOp.ALWAYS_PASS, false, 0.0F, 0.0F, null);
        }
        final DepthStencilState depthStencil = existing;
        if (stencil.equals(depthStencil.stencilTest())) {
            return pipeline;
        }


        return STENCIL_PIPELINE_CACHE
                .computeIfAbsent(pipeline, $ -> new HashMap<>())
                .computeIfAbsent(stencil, s -> {
                    /*
                    26.2
                    RenderPipeline#toBuilder 不会把 activeColorTargetStateCount 复制进 builder
                    直接 build() 会回退到 ColorTargetState.DEFAULT（WRITE_ALL 且无混合），从而丢掉 NO_COLOR_WRITE（颜色掩码）与混合状态
                    这里显式把各颜色目标写回 builder

                    26.3
                    RenderPipeline#toBuilder 已正确复制颜色目标与 activeColorTargetStateCount
                    这里仍显式把各颜色目标写回 builder，是为了避免重蹈 1.21.10 的覆辙
                    （toBuilder 丢颜色目标 -> 回退到 ColorTargetState.DEFAULT -> 丢掉 NO_COLOR_WRITE 颜色掩码与混合状态）
                     */
                    RenderPipeline.Builder builder = pipeline.toBuilder();
                    List<ColorTargetState> colorTargets = pipeline.getColorTargetStates();
                    for (int i = 0; i < colorTargets.size(); i++) {
                        if (colorTargets.get(i) != null) {
                            builder.withColorTargetState(i, colorTargets.get(i));
                        }
                    }
                    return builder
                            .withDepthStencilState(new DepthStencilState(
                                    depthStencil.depthTest(),
                                    depthStencil.writeDepth(),
                                    depthStencil.depthBiasScaleFactor(),
                                    depthStencil.depthBiasConstant(),
                                    s
                            ))
                            .withLocation(pipeline.getLocation().withSuffix("/cgc_stencil"))
                            .build();
                });
    }
}
