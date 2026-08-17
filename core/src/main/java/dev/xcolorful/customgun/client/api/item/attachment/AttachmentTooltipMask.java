/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.item.attachment;

import dev.xcolorful.customgun.client.gui.tooltip.attachment.*;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;

import java.util.EnumSet;

public enum AttachmentTooltipMask implements ResourceTag.MaskTag {
    DESCRIPTION(0, AttachmentDescriptionPart.INSTANCE),
    STATE_INFO(1, AttachmentStateInfoPart.INSTANCE),
    BASE_INFO(2, AttachmentBaseInfoPart.INSTANCE),
    ENCHANTMENT_INFO(3, AttachmentEnchantmentInfoPart.INSTANCE),
    GUIDE_TIP(4, AttachmentGuideTipPart.INSTANCE),
    DETAIL_INFO(5, AttachmentDetailInfoPart.INSTANCE);

    public final String maskName;
    private final int mask;
    /**
     * 从 {@link ClientAttachmentTooltip} 提取而来
     */
    private final AttachmentTooltipPart tooltipPart;
    AttachmentTooltipMask(int ordinal, AttachmentTooltipPart tooltipPart) {
        this.maskName = this.name().toLowerCase();
        this.mask = 1 << ordinal;
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
    public AttachmentTooltipPart getTooltipPart() {
        return this.tooltipPart;
    }
    
    private static final AttachmentTooltipMask[] VALUES = values();

    /**
     * Tooltip 显示掩码
     * <ul>
     *     <li>二进制位为{@code 1}：禁用对应的 Tooltip 部分</li>
     *     <li>二进制位为{@code 0}：启用对应的 Tooltip 部分</li>
     * </ul>
     * 传入{@code 0}表示全部启用
     */
    public static EnumSet<AttachmentTooltipMask> fromBitmap(int bitmap) {
        EnumSet<AttachmentTooltipMask> set = EnumSet.noneOf(AttachmentTooltipMask.class);
        for (AttachmentTooltipMask value : VALUES) {
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
    public static int toBitmap(EnumSet<AttachmentTooltipMask> set) {
        int bitmap = 0;
        for (AttachmentTooltipMask value : VALUES) {
            if (!set.contains(value)) {
                bitmap |= value.mask;
            }
        }
        return bitmap;
    }
}
