/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.gui.tooltip.gun;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
                           GuiGraphicsExtractor guiGraphics,
                           Font font, int pX, int pY) {
    }
    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font, int pX, int pY,
                            int width, int height,
                            GuiGraphicsExtractor guiGraphics) {
    }
}