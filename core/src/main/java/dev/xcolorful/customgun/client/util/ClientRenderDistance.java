/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.config.RenderConfig;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

public class ClientRenderDistance {

    /**
     * 是否显示低模
     */
    public static boolean shouldRenderLod(PoseStack poseStack) {
        // 在GUI界面显示高模
        Minecraft mc = Minecraft.getInstance();
        if (ClientGuiUtils.getCurrentScreen(mc) != null) return false;

        int distance = RenderConfig.GUN_LOD_RENDER_DISTANCE.get();
        if (distance <= 0) return true;

        Matrix4f matrix4f = poseStack.last().pose();
        float viewDistance = matrix4f.m30() * matrix4f.m30() + matrix4f.m31() * matrix4f.m31() + matrix4f.m32() * matrix4f.m32();
        return viewDistance < distance * distance;
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static long GUI_RENDER_TIMESTAMP = -1L;

    @Deprecated(forRemoval = true) public static void markGuiRenderTimestamp() {
        GUI_RENDER_TIMESTAMP = System.currentTimeMillis();
    }
    @Deprecated(forRemoval = true) private static boolean isGuiRender() {
        return System.currentTimeMillis() - GUI_RENDER_TIMESTAMP < 100;
    }
    @Deprecated(forRemoval = true) public static boolean inRenderHighPolyModelDistance(PoseStack poseStack) {
        return !shouldRenderLod(poseStack);
    }
}
