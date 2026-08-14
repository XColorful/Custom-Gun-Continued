/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.GunModelObject;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 原模组的{@link GunModelObject#render}在高版本参数有变，用接口来把变更从 类 提升到 api
 */
public interface IGunModelObjectRender {

    void render(PoseStack matrixStack,
                ItemDisplayContext transformType,
                RenderType renderType,
                int light, int overlay,
                ItemStack gunItem);

    // --------Deprecated--------
}
