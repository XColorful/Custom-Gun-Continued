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

public final class GunAmmoInfoPart implements GunTooltipPart {
    public static final GunAmmoInfoPart INSTANCE = new GunAmmoInfoPart();
    private GunAmmoInfoPart() {}

    @Override
    public void build(ClientGunTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        return 24;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
    }
    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font, int pX, int pY,
                            GuiGraphics guiGraphics) {
    }
}