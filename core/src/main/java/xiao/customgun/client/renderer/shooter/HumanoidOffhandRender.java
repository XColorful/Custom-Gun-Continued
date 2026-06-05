/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.renderer.shooter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;

public class HumanoidOffhandRender {

    public static <S extends ArmedEntityRenderState> void renderGun(S renderState,
                                                                    PoseStack matrixStack, MultiBufferSource buffer, int lightCoords) {
        renderOffhandGun(renderState, matrixStack, buffer, lightCoords);
        renderHotbarGun(renderState, matrixStack, buffer, lightCoords);
    }

    private static <S extends ArmedEntityRenderState> void renderOffhandGun(S renderState,
                                                                            PoseStack matrixStack, MultiBufferSource buffer, int lightCoords) {
        // TODO
    }

    private static <S extends ArmedEntityRenderState> void renderHotbarGun(S renderState,
                                                                           PoseStack matrixStack, MultiBufferSource buffer, int lightCoords) {
        // TODO
    }
}
