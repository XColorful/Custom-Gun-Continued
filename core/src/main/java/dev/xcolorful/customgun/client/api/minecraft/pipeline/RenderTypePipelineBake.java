package dev.xcolorful.customgun.client.api.minecraft.pipeline;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("26.2")
public class RenderTypePipelineBake {

    @ApiStatus.Internal
    public static final String RENDER_TYPE_NAME = "cgc_pipeline_state";
    /**
     * <ul>
     *     26.2 提交 + 延迟渲染
     *     <li>把当前累积的模板测试与管线修饰器（颜色/深度写入等）烘焙进 {@link RenderType}</li>
     *     <li>使几何体在 flush 阶段绘制时仍使用正确的管线</li>
     *     <li>无任何状态时原样返回</li>
     * </ul>
     */
    public static RenderType bakePipelineState(RenderType base) {
        IClientAccessTransformer accessTransformer = CustomGunClient.getAccessTransformer();

        // [1.20.1, 26.2)
        return base;

        // [26.2, )
//        RenderPipeline modified = RenderSystem.applyPipelineModifiers(base.pipeline());
//        if (modified == base.pipeline()) {
//            return base;
//        }
//
//        RenderSetup state = accessTransformer.getState(base);
//        RenderSetup newState = accessTransformer.new_RenderSetup(modified, state);
//        return RenderType.create(RENDER_TYPE_NAME, newState);
    }
}
