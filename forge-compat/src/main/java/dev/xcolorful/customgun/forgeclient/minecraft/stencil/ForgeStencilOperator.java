package dev.xcolorful.customgun.forgeclient.minecraft.stencil;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.api.minecraft.pipeline.PipelineModifier;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilState;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

/*
为了跨版本提前知道1.21.6neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.6")
public class ForgeStencilOperator implements IStencilOperator {

    @Override
    public void applyStencil(StencilState state) {
    }

    @Override
    public void pushPipelineModifier(PipelineModifier modifier) {
    }

    @Override
    public void popPipelineModifier() {
    }

    // ----------1.21.10----------

    /**
     * 1.21.10 移除了 RenderSystem 上的 enableStencil/disableStencil，模板测试改由 {@link RenderPipeline} 携带。
     * 这里把当前要生效的模板测试累积到 ThreadLocal，再由 mixin 在管线应用时注入。
     */
    @ApiStatus.AvailableSince("1.21.10")
    private static final ThreadLocal<Object> CURRENT_STENCIL = new ThreadLocal<>();

    /**
     * 缓存“基础管线 + 模板测试 -> 注入模板后的管线”，保证同一个 {@link RenderPipeline} 对象被复用，
     * 否则 {@code GlDevice#pipelineCache}（IdentityHashMap）每次都会重新编译管线。
     */
    private static final Map<RenderPipeline, Map<Object, RenderPipeline>> STENCIL_PIPELINE_CACHE = new IdentityHashMap<>();

    @ApiStatus.AvailableSince("1.21.10")
    @Override
    public void disableStencil() {
    }

    /**
     * 若当前有模板测试，则把其注入管线并返回；否则原样返回
     */
    @ApiStatus.AvailableSince("1.21.10")
    @ApiStatus.Internal
    public static RenderPipeline applyStencilToPipeline(RenderPipeline pipeline) {
        return null;
    }
}
