package dev.xcolorful.customgun.client.init.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ClientRenderRegistry {

    public static class LaserBeamRenderState {

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
                .withVertexBinding(0, DefaultVertexFormat.PARTICLE)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build();

        public static final RenderPipeline LASER_BEAM_ENTITY_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:laser_beam_entity", CustomGun.MOD_ID)))
                /*
                26.3 的 core/entity.vsh 里 overlayColor = texelFetch(Sampler1, UV1, 0) 不受 EMISSIVE 守卫
                管线没声明 SAMPLER1 就会在预编译时抛 "Unable to find shader defined uniform (Sampler1)" 启动黑屏
                (vanilla 的 ENTITY_TRANSLUCENT_EMISSIVE 同样补了这个 layout)
                 */
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withCull(false)
                .build();

        protected static final RenderType LASER_BEAM = RenderType.create("laser_beam",
                RenderSetup.builder(LASER_BEAM_PIPELINE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                        .useLightmap()
                        .withTexture("Sampler0", LASER_BEAM_TEXTURE)
                        .sortOnUpload()
                        .createRenderSetup());

        protected static final RenderType LASER_BEAM_ENTITY = RenderType.create("laser_beam_entity",
                RenderSetup.builder(LASER_BEAM_ENTITY_PIPELINE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOitPipelines(RenderPipelines.OIT_ENTITY_EMISSIVE)
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

        IrisCompat.registerRenderPipelines(registrar);
    }
}
