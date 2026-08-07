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
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AttachmentRender implements IModelComponentRenderer {

    private final GunModelObject gunModelObject;
    private final AttachmentCategory category;

    public AttachmentRender(GunModelObject gunModelObject, AttachmentCategory category) {
        this.gunModelObject = gunModelObject;
        this.category = category;
    }

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        // TODO
    }

    public static void renderAttachment(PoseStack poseStack,
                                        ItemDisplayContext transformType,
                                        int light, int overlay,
                                        ItemStack gunItem,
                                        ItemStack attachmentItem) {
    }
}
