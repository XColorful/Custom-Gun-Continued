package dev.xcolorful.customgun.client.init.registry;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
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
         *     <li>[1.21.6, ) 不再是程序 json 的 id，而是管线引用的着色器资源 id（{@code shaders/core/<path>.vsh/.fsh}）</li>
         * </ul>
         */
        public static final @NotNull ResourceLocation BAKED_ENTITY_SHADER =
                CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID +
                        // [1.20.1, 1.21.4)
//                        ":cgc_baked_entity"
                        // [1.21.4, )
                        ":core/cgc_baked_entity"
                );

        @Deprecated(since = "1.21.4")
        private static volatile @Nullable Object bakedEntityShader;

        @Deprecated(since = "1.21.6")
        @ApiStatus.AvailableSince("1.21.4")
        private static final @Nullable ShaderProgram bakedEntityProgram =
                BAKED_ENTITY_SHADER == null ? null
                        // [1.20.1, 1.21.4)
//                        : new Object();
                        // [1.21.4, )
//                        new ShaderProgram(BAKED_ENTITY_SHADER, DefaultVertexFormat.NEW_ENTITY, ShaderDefines.EMPTY);
                        new Object();

        @ApiStatus.AvailableSince("1.21.6")
        private static final @NotNull RenderPipeline bakedEntityPipeline =
                        // [1.20.1, 1.21.6)
//                        new Object();
                        // [1.21.6, )
                RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
                        .withLocation(BAKED_ENTITY_SHADER)
                        .withVertexShader(BAKED_ENTITY_SHADER)
                        .withFragmentShader(BAKED_ENTITY_SHADER)
                        .withSampler("Sampler0")
                        .withSampler("Sampler1")
                        .withSampler("Sampler2")
                        .withBlend(BlendFunction.TRANSLUCENT)
                        .withCull(false)
                        .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
                        .build();

        public static @Nullable Object getBakedEntityShader() {
            // [1.20.1, 1.21.4)
//            return bakedEntityShader;

            // [1.21.4, )
//            return Minecraft.getInstance().getShaderManager().getProgram(bakedEntityProgram);
            return null;
        }
        public static @Nullable Object getBakedEntityPipeline() {
            return bakedEntityPipeline;
        }

        /**
         * 按调用方的 {@link RenderType} 派生烘焙管线：保留它的混合 / 剔除 / 深度等状态，只把着色器换成烘焙用的
         * <ul>
         *     <li>烘焙网格是模型空间顶点，vanilla 实体着色器算不对法线与雾，必须换成自己的</li>
         *     <li>但状态不能换：枪体走不透明 {@link RenderType}、镜片走半透明</li>
         *     <li>Iris 正是按这些状态把管线派到对应 program，统一成半透明管线会让不透明几何落进 alpha program，表现为整体透明</li>
         *     <li>{@code toBuilder()} 每调用一次都是新实例，按基础管线缓存，避免每帧重编译</li>
         * </ul>
         */
        @ApiStatus.AvailableSince("1.21.6")
        public static @Nullable RenderPipeline bakedPipelineFor(RenderType renderType) {
            // [1.20.1, 1.21.6)
//            return null;

            // [1.21.6, )
            RenderPipeline base = CustomGunClient.getAccessTransformer().getRenderPipeline(renderType);
            return BAKED_PIPELINES.computeIfAbsent(base, pipeline -> pipeline.toBuilder()
                    .withVertexShader(BAKED_ENTITY_SHADER)
                    .withFragmentShader(BAKED_ENTITY_SHADER)
                    .build());
        }

        private static final Map<RenderPipeline, RenderPipeline> BAKED_PIPELINES = new IdentityHashMap<>();
    }

    @FunctionalInterface
    public interface IShaderRegistrar {
        /**
//         * @param location 形如 {@code customgun:cgc_baked_entity}，对应 {@code assets/<ns>/shaders/core/<path>.json}
//         * @param onLoaded 加载成功后的回调（每次资源重载都会调用）
         * @param program 对应 {@code assets/<ns>/shaders/core/<path>.json}；编译产物由平台的着色器管理器持有
         */
        void registerShader(
                // [1.20.1, 1.21.4)
//                @NotNull ResourceLocation location,
//                @NotNull VertexFormat format,
//                @NotNull Consumer<ShaderInstance> onLoaded

                // [1.21.4, )
                @NotNull Object program
        );
    }

    /**
     * <ul>
     *     [1.20.1, 1.21.4)
     *     <li>资源重载会重新创建 {@code ShaderInstance}，所以每次都要重新回调并覆盖引用</li>
     * </ul>
     * <ul>
     *     [1.21.4, )
     *     <li>着色器以「定义」注册，编译产物由 {@code ShaderManager} 每次重载重建/li>
     *     <li>资源重载后编译产物会被重建，因此这里只登记定义，取用时再到管理器里查</li>
     * </ul>
     */
    @Deprecated(since = "1.21.6")
    public static void onRegisterShaders(IShaderRegistrar registrar) {
        // [1.20.1, 1.21.4)
//        @Nullable var location = BakedEntityRenderState.BAKED_ENTITY_SHADER;
//        if (location == null) return;
//
//        registrar.registerShader(location,
//                DefaultVertexFormat.NEW_ENTITY,
//                shader -> BakedEntityRenderState.bakedEntityShader = shader);

        // [1.21.4, )
        @Nullable var program = BakedEntityRenderState.bakedEntityProgram;
        if (program == null) return;

        registrar.registerShader(program);
    }

    public static class LaserBeamRenderState extends RenderStateShard {

        public static final @NotNull Identifier LASER_BEAM_TEXTURE = CustomTexture.WHITE_8x8.getLocation();

        public LaserBeamRenderState(String pName, Runnable pSetupState, Runnable pClearState) {
        }

        public static final RenderPipeline LASER_BEAM_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:laser_beam", CustomGun.MOD_ID)))
                .withVertexShader("core/particle")
                .withFragmentShader("core/particle")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                /*
                [26.1.x, ) 不写这条时管线没有 depthStencilState (26.1 起 Builder 的默认值从「LEQUAL + 写深度」变成 null)
                而 null 在 applyPipelineState 里等价于关闭深度测试
                于是光束无视枪体直接画在最上层；1.21.11 及以前靠 Builder 默认值自带深度测试
                 */
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .withCull(false)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build();

        public static final RenderPipeline LASER_BEAM_ENTITY_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:laser_beam_entity", CustomGun.MOD_ID)))
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withCull(false)
                .build();

        protected static final RenderType LASER_BEAM = RenderType.create("laser_beam",
                RenderSetup.builder(LASER_BEAM_PIPELINE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .useLightmap()
                        .withTexture("Sampler0", LASER_BEAM_TEXTURE)
                        .sortOnUpload()
                        .createRenderSetup());

        protected static final RenderType LASER_BEAM_ENTITY = RenderType.create("laser_beam_entity",
                RenderSetup.builder(LASER_BEAM_ENTITY_PIPELINE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .useOverlay()
                        .withTexture("Sampler0", LASER_BEAM_TEXTURE)
                        .sortOnUpload()
                        .createRenderSetup());

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
        @ApiStatus.Internal
        public static final RenderPipeline MODEL_TEXT_PIPELINE = RenderPipeline.builder(RenderPipelines.WORLD_TEXT_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:model_text", CustomGun.MOD_ID)))
                .withVertexShader("core/text")
                .withFragmentShader("core/text")
                .build();

        /**
         * 按 vanilla 文字 RenderType（每个字体图集页一条）缓存烘焙结果
         */
        private static final Map<RenderType, RenderType> CACHE = new HashMap<>();

        public static RenderType getModelText(RenderType vanillaTextRenderType) {
            return CACHE.computeIfAbsent(vanillaTextRenderType, renderType -> {
                IClientAccessTransformer accessTransformer = CustomGunClient.getAccessTransformer();
                return RenderType.create("cgc_model_text",
                        accessTransformer.new_RenderSetup(MODEL_TEXT_PIPELINE, accessTransformer.getState(renderType)));
            });
        }
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

        registrar.accept(BakedEntityRenderState.bakedEntityPipeline);
        registrar.accept(LaserBeamRenderState.LASER_BEAM_PIPELINE);
        registrar.accept(LaserBeamRenderState.LASER_BEAM_ENTITY_PIPELINE);

        // [26.2)
        registrar.accept(ModelTextRenderState.MODEL_TEXT_PIPELINE);

        IrisCompat.registerRenderPipelines(registrar);
    }
}
