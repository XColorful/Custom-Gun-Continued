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
 * 这个类是默认弃用的，留给扩展模组mixin，理由如下：
 * <ul>
 *     <li>穿甲可以算基础信息，放在{@link GunBaseInfoPart}注入</li>
 *     <li>配件加成/附魔可以放在此类注入</li>
 *     <li>如果有模组动态修改穿甲或其他属性，则需要具有时效性的tooltip绘制方式</li>
 *     <li>如果游戏玩法不想让玩家知道信息/更沉浸于description，则此类tooltip应该隐藏</li>
 * </ul>
 */
public final class GunEnchantmentInfoPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunEnchantmentInfoPart INSTANCE = new GunEnchantmentInfoPart();
    private GunEnchantmentInfoPart() {}

    @Override
    public void build(ClientGunTooltip.Context context, Font font) {
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.ENCHANTMENT_INFO)) return 0;

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
