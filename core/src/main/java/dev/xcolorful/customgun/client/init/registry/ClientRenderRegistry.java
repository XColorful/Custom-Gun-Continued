/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.init.registry;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
                .withVertexShader("core/position_color_tex_lightmap")
                .withFragmentShader("core/position_color_tex_lightmap")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
//                .withBlend(BlendFunction.LIGHTNING)
                .withCull(false)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build();

        public static final RenderPipeline LASER_BEAM_ENTITY_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:laser_beam_entity", CustomGun.MOD_ID)))
//                .withBlend(BlendFunction.LIGHTNING)
                .withCull(false)
                .build();

        public static RenderType getLaserBeam() {
            return RenderTypes.entityTranslucent(LASER_BEAM_TEXTURE);
        }

        public static RenderType getLaserBeamEntity() {
            return RenderTypes.entityTranslucent(LASER_BEAM_TEXTURE);
        }
    }

    @ApiStatus.AvailableSince("1.21.6")
    public static void onRegisterRenderPipelines(Consumer<RenderPipeline> registrar) {
        if (true) return;

        registrar.accept(LaserBeamRenderState.LASER_BEAM_PIPELINE);
        registrar.accept(LaserBeamRenderState.LASER_BEAM_ENTITY_PIPELINE);

        // TODO IrisShaders register
    }
}
