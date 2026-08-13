/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.api.event.IComputeCameraAnglesEvent;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface IAnimateGeoItemRendererOperator {

    /**
     * 应用状态机的世界摄像机动画，暂时只用于玩家
     */
    void applyLevelCameraAnimation(IComputeCameraAnglesEvent event, ItemStack pojoItem, LocalPlayer player);

    /**
     * 应用状态机的手持物品摄像机动画，暂时只用于玩家
     */
    void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack pojoItem, LocalPlayer player);

    /**
     * 渲染第一人称，暂时只用于玩家
     */
    void renderFirstPerson(PoseStack poseStack,
                           MultiBufferSource bufferSource,
                           ItemDisplayContext ctx,
                           int light, float partialTick,
                           LocalPlayer player, ItemStack pojoItem);
}
