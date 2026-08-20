/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import net.minecraft.client.gui.Font;

/**
 * 这个按键提示没太大必要，留给扩展模组mixin，动态切换提示文本喵~
 * <ul>
 *     <li>可以做成新手教学动态显示/取消显示</li>
 *     <li>可以常驻在GUI上，而不是画在物品tooltip里</li>
 * </ul>
 */
public final class GunGuideTipPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunGuideTipPart INSTANCE = new GunGuideTipPart();
    private GunGuideTipPart() {}

    @Override
    public void build(ClientGunTooltip.Context context, Font font) {
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.GUIDE_TIP)) return 0;

        return 0;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           int startX, int startY) {
        // mixin注入点
    }

    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            int startX, int startY) {
        // mixin注入点
    }
}
