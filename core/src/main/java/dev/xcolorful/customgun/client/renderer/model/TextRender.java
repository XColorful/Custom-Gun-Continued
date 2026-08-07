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
import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TextRender implements IModelComponentRenderer {

    private final ModelObject modelObject;
    private final _ModelNodeTextDisplay modelNodeTextDisplay;
    private final ItemStack gunItem;

    public TextRender(ModelObject modelObject, _ModelNodeTextDisplay modelNodeTextDisplay, ItemStack gunItem) {
        this.modelObject = modelObject;
        this.modelNodeTextDisplay = modelNodeTextDisplay;
        this.gunItem = gunItem;
    }

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        // TODO
    }
}
