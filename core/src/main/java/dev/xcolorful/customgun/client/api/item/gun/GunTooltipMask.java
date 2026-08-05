/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.item.gun;

import dev.xcolorful.customgun.client.gui.tooltip.gun.*;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;

import java.util.EnumSet;

public enum GunTooltipMask implements ResourceTag.MaskTag {
    DESCRIPTION(GunDescriptionPart.INSTANCE),
    AMMO_INFO(GunAmmoInfoPart.INSTANCE),
    BASE_INFO(GunBaseInfoPart.INSTANCE),
    EXTRA_DAMAGE_INFO(GunExtraDamageInfoPart.INSTANCE),
    UPGRADES_TIP(GunUpgradesTipPart.INSTANCE),
    PACK_INFO(GunPackInfoPart.INSTANCE);

    public final String maskName;
    private final int mask;
    /**
     * 从 {@link ClientGunTooltip} 提取而来
     */
    private final GunTooltipPart tooltipPart;
    GunTooltipMask(GunTooltipPart tooltipPart) {
        this.maskName = this.name().toLowerCase();
        this.mask = 1 << this.ordinal();
        this.tooltipPart = tooltipPart;
    }
    @Override
    public String getTagName() {
        return this.maskName;
    }
    @Override
    public int getMask() {
        return this.mask;
    }
    public GunTooltipPart getTooltipPart() {
        return this.tooltipPart;
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
}
