package dev.xcolorful.customgun.client.api.minecraft.pipeline;

//import com.mojang.blaze3d.pipeline.RenderPipeline; // 1.21.4没有RenderPipeline
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import dev.xcolorful.customgun.client.model.bake.StaticBakeCache;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

@ApiStatus.AvailableSince("26.2")
public class RenderTypePipelineBake {

    @ApiStatus.Internal
    public static final String RENDER_TYPE_NAME = "cgc_pipeline_state";

    /**
     * 包装后的 RenderType 缓存：基础 RenderType → 修饰后管线 → 包装
     * <ul>
     *     26.2 提交 + 延迟渲染
     *     <li>缓存键以 RenderType 实例为身份，烘焙缓存（{@link StaticBakeCache}）同样按实例建键</li>
     *     <li>每次调用都新建包装会让烘焙键每帧翻新，把共享缓存撑满并触发淘汰，静态几何整片消失</li>
     *     <li>管线修饰器本身已按「基础管线 + 模板状态」缓存，这里只需保证包装实例稳定</li>
     * </ul>
     */
    private static final Map<RenderType, Map<Object, RenderType>> BAKED_TYPES = new IdentityHashMap<>();

    /**
     * 包装种类是有限的（基础 RenderType × 模板状态），超过这个数只可能是资源重载后换了实例
     */
    private static final int MAX_CACHED_TYPES = 64;

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

        // [26.2, 26.3)
//        RenderPipeline modified = RenderSystem.applyPipelineModifiers(base.pipeline());
        // [26.3, )
        // 26.3去掉了 RenderSystem#applyPipelineModifiers，改从私有修饰器栈取被修饰后的管线
//        RenderPipeline modified = accessTransformer.RenderSystem_getPIPELINE_MODIFIERS_apply(base.pipeline());

        // [26.2, )
        /*
        Map<RenderPipeline, RenderType> perBase = BAKED_TYPES.get(base);
        if (perBase == null) {
            if (BAKED_TYPES.size() >= MAX_CACHED_TYPES) BAKED_TYPES.clear();
            perBase = new IdentityHashMap<>();
            BAKED_TYPES.put(base, perBase);
        }
        RenderType baked = perBase.get(modified);
        if (baked == null) {
            RenderSetup state = accessTransformer.getState(base);
            RenderSetup newState = accessTransformer.new_RenderSetup(modified, state);
            baked = RenderType.create(RENDER_TYPE_NAME, newState);
            perBase.put(modified, baked);
        }
        return baked;
         */
    }
}
