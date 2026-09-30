package dev.xcolorful.customgun.client.init.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
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
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                /*
                [26.1.x, ) 不写这条时管线没有 depthStencilState (26.1 起 Builder 的默认值从「LEQUAL + 写深度」变成 null)
                而 null 在 applyPipelineState 里等价于关闭深度测试
                于是光束无视枪体直接画在最上层；1.21.11 及以前靠 Builder 默认值自带深度测试
                 */
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS)
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
