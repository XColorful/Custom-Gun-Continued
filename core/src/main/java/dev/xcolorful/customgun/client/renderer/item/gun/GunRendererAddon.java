/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.renderer.item.GunItemRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 负责第一人称的枪械模型额外效果的渲染
 * <ul>
 *     <li>主体部分见 {@link GunItemRenderer}</li>
 *     <li>暂时想不出比{@link GunRendererAddon}更合适的名字</li>
 * </ul>
 */
public class GunRendererAddon {

    public static void applyFirstPersonGunTransform(PoseStack poseStack, float partialTicks, GunModelObject model, LocalPlayer player, ItemStack gunItem) {
        // TODO
    }
}
