/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.ModelObject;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 原模组的{@link ModelObject#render}在高版本参数有变，用接口来把变更从 类 提升到 api
 */
public interface IModelObjectRender {

    default void render(PoseStack matrixStack,
                ItemDisplayContext transformType,
                RenderType renderType,
                int light, int overlay) {
        this.render(matrixStack,
                transformType,
                renderType,
                light, overlay,
                1.0F, 1.0F, 1.0F, 1.0F);
    }

    void render(PoseStack matrixStack,
                ItemDisplayContext transformType,
                RenderType renderType,
                int light, int overlay,
                float red, float green, float blue, float alpha);
}
