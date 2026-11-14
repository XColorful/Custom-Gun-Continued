package dev.xcolorful.customgun.client.init.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import net.minecraft.client.renderer.RenderPipelines;
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
     * 静态烘焙网格用的 core shader（渲染重构B1-a）
     * <br>
     * 无光影：
     * <ul>
     *     <li>原版实体着色器用原始 {@code Normal} 做光照、用 {@code IViewRotMat * Position} 算雾，两者都假设顶点已经是视图空间</li>
     *     <li>烘焙网格是模型空间 + 实例矩阵，必须换成会把 {@code ModelViewMat} 一起算进法线与雾坐标的着色器，见 {@code assets/customgun/shaders/core/cgc_baked_entity.*}</li>
     * </ul>
     * 开光影包：
     * <ul>
     *     <li>改由 Oculus/Iris 在 {@code GameRenderer.getRendertype*Shader()} 换成的光影包 program 绘制</li>
     * </ul>
     */
    public static class BakedEntityRenderState {

        /**
         * <ul>
         *     <li>[1.20.1, 1.21.4) 程序 id 无需自带文件夹</li>
         *     <li>[1.21.4, ) 程序 id 由 {@code shaders/core/<path>.json} 的路径推出，必须带 {@code core/} 前缀</li>
         * </ul>
         */
        public static final @Nullable ResourceLocation BAKED_ENTITY_SHADER =
                CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID +
                        // [1.20.1, 1.21.4)
                        ":cgc_baked_entity"
                        // [1.21.4, )
//                        ":core/cgc_baked_entity"
                );

//        @Deprecated(since = "1.21.4")
        private static volatile @Nullable ShaderInstance bakedEntityShader;
        @ApiStatus.AvailableSince("1.21.4")
        private static final @Nullable Object bakedEntityProgram =
                BAKED_ENTITY_SHADER == null ? null
                        // [1.20.1, 1.21.4)
                        : new Object();
                        // [1.21.4, )
//                        : new ShaderProgram(BAKED_ENTITY_SHADER, DefaultVertexFormat.NEW_ENTITY, ShaderDefines.EMPTY);

        public static @Nullable ShaderInstance getBakedEntityShader() {
            // [1.20.1, 1.21.4)
            return bakedEntityShader;

            // [1.21.4, )
//            @Nullable ShaderProgram program = bakedEntityProgram;
//            return program == null ? null : Minecraft.getInstance().getShaderManager().getProgram(program);
        }
    }

    @FunctionalInterface
    public interface IShaderRegistrar {
        /**
         * @param location 形如 {@code customgun:cgc_baked_entity}，对应 {@code assets/<ns>/shaders/core/<path>.json}
         * @param onLoaded 加载成功后的回调（每次资源重载都会调用）
//         * @param program 对应 {@code assets/<ns>/shaders/core/<path>.json}；编译产物由平台的着色器管理器持有
         */
        void registerShader(
                // [1.20.1, 1.21.4)
                @NotNull ResourceLocation location,
                @NotNull VertexFormat format,
                @NotNull Consumer<ShaderInstance> onLoaded

                // [1.21.4, )
//                @NotNull ShaderProgram program
        );
    }

    /**
     * <ul>
     *     [1.20.1, 1.21.4)
     *     <li>资源重载会重新创建 {@link ShaderInstance}，所以每次都要重新回调并覆盖引用</li>
     * </ul>
     * <ul>
     *     [1.21.4, )
     *     <li>着色器以「定义」注册，编译产物由 {@code ShaderManager} 每次重载重建/li>
     *     <li>资源重载后编译产物会被重建，因此这里只登记定义，取用时再到管理器里查</li>
     * </ul>
     */
    public static void onRegisterShaders(IShaderRegistrar registrar) {
        // [1.20.1, 1.21.4)
        @Nullable var location = BakedEntityRenderState.BAKED_ENTITY_SHADER;
        if (location == null) return;

        registrar.registerShader(location,
                DefaultVertexFormat.NEW_ENTITY,
                shader -> BakedEntityRenderState.bakedEntityShader = shader);

        // [1.21.4, )
//        @Nullable var program = BakedEntityRenderState.bakedEntityProgram;
//        if (program == null) return;
//
//        registrar.registerShader(program);
    }

    public static class LaserBeamRenderState extends RenderStateShard {

        public static final @NotNull ResourceLocation LASER_BEAM_TEXTURE = CustomTexture.WHITE_8x8.getLocation();

        public LaserBeamRenderState(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }

        public static final RenderPipeline LASER_BEAM_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:laser_beam", CustomGun.MOD_ID)))
                .withVertexShader("core/particle")
                .withFragmentShader("core/particle")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withBlend(BlendFunction.LIGHTNING)
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS)
                .build();

        public static final RenderPipeline LASER_BEAM_ENTITY_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:laser_beam_entity", CustomGun.MOD_ID)))
                .withBlend(BlendFunction.LIGHTNING)
                .withCull(false)
                .build();

        protected static final RenderType LASER_BEAM = RenderType.create(
                "laser_beam",
                256,
                true,
                true,
                LASER_BEAM_PIPELINE,
                RenderType.CompositeState.builder()
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setLightmapState(LIGHTMAP)
                        .setTextureState(new RenderStateShard.TextureStateShard(LASER_BEAM_TEXTURE, false))
                        .createCompositeState(false));

        protected static final RenderType LASER_BEAM_ENTITY = RenderType.create(
                "laser_beam_entity",
                256,
                true,
                true,
                LASER_BEAM_ENTITY_PIPELINE,
                RenderType.CompositeState.builder()
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setTextureState(new RenderStateShard.TextureStateShard(LASER_BEAM_TEXTURE, false))
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

        registrar.accept(LaserBeamRenderState.LASER_BEAM_PIPELINE);
        registrar.accept(LaserBeamRenderState.LASER_BEAM_ENTITY_PIPELINE);

        // [26.2)
//        registrar.accept(ModelTextRenderState.MODEL_TEXT_PIPELINE);

        IrisCompat.registerRenderPipelines(registrar);
    }
}
