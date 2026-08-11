/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.compat.ar;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BeamRenderAR {

    /**
     * @return 是否接管渲染
     */
    public static boolean render(PoseStack poseStack,
                                 ItemDisplayContext transformType,
                                 @NotNull List<BedrockPart> path,
                                 ItemStack pojoItem) {
        return false;
    }
}
