package dev.xcolorful.customgun.client.init.registry;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.minecraft.shader.ICoreShaderRegistrar;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ClientRenderRegistry {

    /**
     * 静态烘焙网格（B1-a）用的 core shader
     * <ul>
     *     <li><b>无光影</b>：原版实体着色器用原始 {@code Normal} 做光照、用 {@code IViewRotMat * Position} 算雾，
     *     两者都假设顶点已经是视图空间；烘焙网格是模型空间 + 实例矩阵，必须换成会把 {@code ModelViewMat}
     *     一起算进法线与雾坐标的着色器，见 {@code assets/customgun/shaders/core/cgc_baked_entity.*}</li>
     *     <li><b>开光影包</b>：不使用它，改由 Oculus/Iris 在 {@code GameRenderer.getRendertype*Shader()}
     *     换成的光影包 program 绘制</li>
     * </ul>
     */
    public static class BakedEntityRenderState {

        public static final @Nullable ResourceLocation BAKED_ENTITY_SHADER =
                CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":cgc_baked_entity");

        private static volatile @Nullable ShaderInstance bakedEntityShader;

        public static @Nullable ShaderInstance getBakedEntityShader() {
            return bakedEntityShader;
        }
    }

    /**
     * 由平台在着色器注册事件里调用（Forge: {@code RegisterShadersEvent}）
     * <p>
     * 资源重载会重新创建 {@link ShaderInstance}，所以每次都要重新回调并覆盖引用。
     */
    public static void onRegisterShaders(ICoreShaderRegistrar registrar) {
        @Nullable ResourceLocation location = BakedEntityRenderState.BAKED_ENTITY_SHADER;
        if (location == null) return;

        registrar.registerCoreShader(location,
                DefaultVertexFormat.NEW_ENTITY,
                shader -> BakedEntityRenderState.bakedEntityShader = shader);
    }

    public static class LaserBeamRenderState extends RenderStateShard {

        public static final @NotNull ResourceLocation LASER_BEAM_TEXTURE = CustomTexture.WHITE_8x8.getLocation();

        public LaserBeamRenderState(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }

        public static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
                "lightning_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        protected static final boolean enableBlur = false; // TriState.FALSE

        protected static final RenderType LASER_BEAM = RenderType.create("laser_beam", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, true, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setLightmapState(LIGHTMAP)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .setTextureState(new RenderStateShard.TextureStateShard(LASER_BEAM_TEXTURE,
                                enableBlur,
                                false))
                        .createCompositeState(false));

        protected static final RenderType LASER_BEAM_ENTITY = RenderType.create("laser_beam_entity", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .setTextureState(new RenderStateShard.TextureStateShard(LASER_BEAM_TEXTURE,
                                enableBlur,
                                false))
                        .createCompositeState(false));

        public static RenderType getLaserBeam() {
            return LASER_BEAM;
        }

        public static RenderType getLaserBeamEntity() {
            return LASER_BEAM_ENTITY;
        }
    }

    /**
     * 模型文字用的管线与 RenderType
     * <ul>
     *     <li>配方与 vanilla 的 {@code pipeline/text} 逐字相同，只换 location</li>
     *     <li>于是 Iris 不会把它当成手部程序的一条，光影下由我们自己的着色器执行</li>
     *     <li>RenderType 由 vanilla 的文字 RenderType 换管线烘焙而来：字体图集页、lightmap 这些绑定原样保留，只把管线替成 {@code ClientRenderRegistry.MODEL_TEXT_PIPELINE}</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("26.2")
    public static class ModelTextRenderState {

        /**
         * [26.2, ) 文字管线刻意不交给 IrisCompat.registerRenderPipelines 去 assign
         */
//        @ApiStatus.Internal
//        public static final RenderPipeline MODEL_TEXT_PIPELINE = RenderPipeline.builder(RenderPipelines.WORLD_TEXT_SNIPPET)
//                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:model_text", CustomGun.MOD_ID)))
//                .withVertexShader("core/text")
//                .withFragmentShader("core/text")
//                .build();

        /**
         * 按 vanilla 文字 RenderType（每个字体图集页一条）缓存烘焙结果
         */
//        private static final Map<RenderType, RenderType> CACHE = new HashMap<>();

//        public static RenderType getModelText(RenderType vanillaTextRenderType) {
//            return CACHE.computeIfAbsent(vanillaTextRenderType, renderType -> {
//                IClientAccessTransformer accessTransformer = CustomGunClient.getAccessTransformer();
//                return RenderType.create("cgc_model_text",
//                        accessTransformer.new_RenderSetup(MODEL_TEXT_PIPELINE, accessTransformer.getState(renderType)));
//            });
//        }
    }

    /**
     * <ul>
     *     需要进游戏实测的测试项：
     *     <li>1.21.6+：激光是否渲染</li>
     *     <li>1.21.10+：启动是否黑屏 (取消注册可以临时解决)</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("1.21.6")
    public static void onRegisterRenderPipelines(Consumer<RenderPipeline> registrar) {

//        registrar.accept(LaserBeamRenderState.LASER_BEAM_PIPELINE);
//        registrar.accept(LaserBeamRenderState.LASER_BEAM_ENTITY_PIPELINE);

        // [26.2)
//        registrar.accept(ModelTextRenderState.MODEL_TEXT_PIPELINE);

        IrisCompat.registerRenderPipelines(registrar);
    }
}
