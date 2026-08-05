/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.model.IModelComponentRenderer;
import net.minecraft.world.item.ItemDisplayContext;

public class ShellRender implements IModelComponentRenderer {

    public static boolean isSelf = false;

    @Override
    public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
        // TODO
    }
}
