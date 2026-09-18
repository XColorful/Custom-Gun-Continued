package dev.xcolorful.customgun.core.api.item.ammo;

import net.minecraft.world.item.ItemStack;

public interface IAmmoExpAccess {

    /**
     * 获取子弹等级
     */
    int getAmmoLevel(ItemStack ammoItem);
    void setAmmoLevel(ItemStack ammoItem, int ammoLevel);
}
