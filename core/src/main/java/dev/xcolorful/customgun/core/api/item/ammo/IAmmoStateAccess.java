package dev.xcolorful.customgun.core.api.item.ammo;

import net.minecraft.world.item.ItemStack;

public interface IAmmoStateAccess {

    /**
     * 获取tooltip掩码 (服务端处理数据，客户端读取)
     */
    boolean hasTooltipMask(ItemStack ammoItem);
    int getTooltipMask(ItemStack ammoItem);
    void setTooltipMask(ItemStack ammoItem, int tooltipMask);
}