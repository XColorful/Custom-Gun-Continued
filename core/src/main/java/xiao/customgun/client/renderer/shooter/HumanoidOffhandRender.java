/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.renderer.shooter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidOffhandRender {

    public static void renderGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int lightCoords) {
        renderOffhandGun(entity, matrixStack, buffer, lightCoords);
        renderHotbarGun(entity, matrixStack, buffer, lightCoords);
    }

    private static void renderOffhandGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int lightCoords) {
        // TODO
    }

    private static void renderHotbarGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int lightCoords) {
        // TODO
    }
}
