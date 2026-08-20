package dev.xcolorful.customgun.client.api.item.ammobox;

import dev.xcolorful.customgun.client.gui.tooltip.ammobox.*;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;

import java.util.EnumSet;

public enum AmmoBoxTooltipMask implements ResourceTag.MaskTag {
    DESCRIPTION(0, AmmoBoxDescriptionPart.INSTANCE),
    STATE_INFO(1, AmmoBoxStateInfoPart.INSTANCE),
    BASE_INFO(2, AmmoBoxBaseInfoPart.INSTANCE),
    ENCHANTMENT_INFO(3, AmmoBoxEnchantmentInfoPart.INSTANCE),
    GUIDE_TIP(4, AmmoBoxGuideTipPart.INSTANCE),
    DETAIL_INFO(5, AmmoBoxDetailInfoPart.INSTANCE),;

    public final String maskName;
    private final int mask;
    private final AmmoBoxTooltipPart tooltipPart;
    AmmoBoxTooltipMask(int ordinal, AmmoBoxTooltipPart tooltipPart) {
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
    
    public AmmoBoxTooltipPart getTooltipPart() {
        return this.tooltipPart;
    }
    
    private static final AmmoBoxTooltipMask[] VALUES = values();

    /**
     * Tooltip 显示掩码
     * <ul>
     *     <li>二进制位为{@code 1}：禁用对应的 Tooltip 部分</li>
     *     <li>二进制位为{@code 0}：启用对应的 Tooltip 部分</li>
     * </ul>
     * 传入{@code 0}表示全部启用
     */
    public static EnumSet<AmmoBoxTooltipMask> fromBitmap(int bitmap) {
        EnumSet<AmmoBoxTooltipMask> set = EnumSet.noneOf(AmmoBoxTooltipMask.class);
        for (AmmoBoxTooltipMask value : VALUES) {
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
    public static int toBitmap(EnumSet<AmmoBoxTooltipMask> set) {
        int bitmap = 0;
        for (AmmoBoxTooltipMask value : VALUES) {
            if (!set.contains(value)) {
                bitmap |= value.mask;
            }
        }
        return bitmap;
    }
}
