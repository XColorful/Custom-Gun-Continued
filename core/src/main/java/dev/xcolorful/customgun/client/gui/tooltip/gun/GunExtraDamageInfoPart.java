/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class GunExtraDamageInfoPart implements GunTooltipPart {
    public static final GunExtraDamageInfoPart INSTANCE = new GunExtraDamageInfoPart();
    private GunExtraDamageInfoPart() {}

    @Override
    public void build(ClientGunTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        return 34;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           GuiGraphics guiGraphics,
                           Font font, int pX, int pY) {
    }
    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font, int pX, int pY,
                            int width, int height,
                            GuiGraphics guiGraphics) {
    }
}