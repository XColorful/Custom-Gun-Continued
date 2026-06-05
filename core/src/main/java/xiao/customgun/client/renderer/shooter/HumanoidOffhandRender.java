/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.renderer.shooter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;

public class HumanoidOffhandRender {

    public static <S extends ArmedEntityRenderState> void renderGun(S renderState,
                                                                    PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        renderOffhandGun(renderState, matrixStack, submitNodeCollector, lightCoords);
        renderHotbarGun(renderState, matrixStack, submitNodeCollector, lightCoords);
    }

    private static <S extends ArmedEntityRenderState> void renderOffhandGun(S renderState,
                                                                            PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        // TODO
    }

    private static <S extends ArmedEntityRenderState> void renderHotbarGun(S renderState,
                                                                           PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        // TODO
    }
}
