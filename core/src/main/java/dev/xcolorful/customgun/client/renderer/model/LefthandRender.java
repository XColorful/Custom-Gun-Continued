/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.AnimatedModelObject;
import net.minecraft.world.item.ItemDisplayContext;

public class LefthandRender implements IModelComponentRenderer {

    private final AnimatedModelObject animatedModelObject;

    public LefthandRender(AnimatedModelObject animatedModelObject) {
        this.animatedModelObject = animatedModelObject;
    }

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        // TODO
    }
}
