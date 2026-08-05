/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public final class GunDescriptionPart implements GunTooltipPart {
    public static final GunDescriptionPart INSTANCE = new GunDescriptionPart();
    private GunDescriptionPart() {}

    @Override
    public void build(ClientGunTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        var desc = context.view.desc;
        if (desc != null && !desc.isEmpty()) {
            return 10 * desc.size() + 2;
        }
        return 0;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
    }
    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font, int pX, int pY,
                            int width, int height,
                            GuiGraphics guiGraphics) {
    }
}
