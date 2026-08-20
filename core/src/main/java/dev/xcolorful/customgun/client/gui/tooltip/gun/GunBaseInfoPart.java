/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * 这个类有用但不多，display的damage只是摆设，没法表示{@link _DistanceDamageData}距离衰减
 * 可以考虑放在扩展模组里定义格式，比如简单的直接列出，复杂的取50m/100m/200m伤害
 */
public final class GunBaseInfoPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunBaseInfoPart INSTANCE = new GunBaseInfoPart();
    private GunBaseInfoPart() {}

    @Override
    public void build(ClientGunTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.BASE_INFO)) return 0;

        return 0;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font,
                           int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        // mixin注入点
    }

    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font,
                            int pX, int pY,
                            int width, int height,
                            GuiGraphics guiGraphics) {
        // mixin注入点
    }
}
