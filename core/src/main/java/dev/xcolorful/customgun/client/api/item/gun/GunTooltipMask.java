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

    /**
     * Tooltip 显示掩码
     * <ul>
     *     <li>二进制位为{@code 1}：禁用对应的 Tooltip 部分</li>
     *     <li>二进制位为{@code 0}：启用对应的 Tooltip 部分</li>
     * </ul>
     * 传入{@code 0}表示全部启用
     */
    public static EnumSet<GunTooltipMask> fromBitmap(int bitmap) {
        EnumSet<GunTooltipMask> set = EnumSet.noneOf(GunTooltipMask.class);
        for (GunTooltipMask value : VALUES) {
            if ((bitmap & value.mask) == 0) {
                set.add(value);
            }
        }
        return set;
    }

    /**
     * 根据启用的 Tooltip 部分生成显示掩码
     * <ul>
     *     <li>集合中包含：对应二进制位为{@code 0}</li>
     *     <li>集合中不包含：对应二进制位为{@code 1}</li>
     * </ul>
     * @param set 启用的 Tooltip 部分
     * @return Tooltip 显示掩码
     */
    public static int toBitmap(EnumSet<GunTooltipMask> set) {
        int bitmap = 0;
        for (GunTooltipMask value : VALUES) {
            if (!set.contains(value)) {
                bitmap |= value.mask;
            }
        }
        return bitmap;
    }
}
