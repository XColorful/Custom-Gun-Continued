/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.item.gun;

import xiao.customgun.client.gui.tooltip.ClientGunTooltip;import java.util.EnumSet;

public enum GunTooltipMask {
    DESCRIPTION(10),
    AMMO_INFO(24),
    BASE_INFO(34),
    EXTRA_DAMAGE_INFO(34),
    UPGRADES_TIP(14),
    PACK_INFO(14);

    private final int mask;
    /**
     * 从 {@link ClientGunTooltip} 提取而来
     */
    private final int baseHeight;
    GunTooltipMask(int baseHeight) {
        this.mask = 1 << this.ordinal();
        this.baseHeight = baseHeight;
    }
    public int getMask() {
        return this.mask;
    }
    public int getBaseHeight() {
        return this.baseHeight;
    }

    private static final GunTooltipMask[] VALUES = values();

    public static EnumSet<GunTooltipMask> fromBitmap(int bitmap) {
        EnumSet<GunTooltipMask> set = EnumSet.noneOf(GunTooltipMask.class);
        for (GunTooltipMask value : VALUES) {
            if ((bitmap & value.mask) != 0) {
                set.add(value);
            }
        }
        return set;
    }

    public static int toBitmap(EnumSet<GunTooltipMask> set) {
        int bitmap = 0;
        for (GunTooltipMask value : set) {
            bitmap |= value.mask;
        }
        return bitmap;
    }

    /**
     * 从 {@link ClientGunTooltip} 提取而来
     */
    public static int calculateHeight(ClientGunTooltip.DrawTooltipContext context) {
        int height = 0;
        EnumSet<GunTooltipMask> visibleParts = context.visibleParts();
        for (GunTooltipMask part : visibleParts) {
            height += part.baseHeight;
        }

        // description动态高度
        if (visibleParts.contains(DESCRIPTION)) {
            height -= DESCRIPTION.baseHeight;

            var desc = context.desc();
            if (desc != null && !desc.isEmpty()) {
                height += DESCRIPTION.baseHeight * desc.size() + 2;
            }
        }
        return height;
    }
}
