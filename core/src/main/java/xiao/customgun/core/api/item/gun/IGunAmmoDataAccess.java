/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IGunAmmoDataAccess {

    /**
     * 为一次射击消耗一次子弹
     * @return 返回消耗的子弹数，为0则不消耗子弹
     */
    int consumeAmmoOnce(ItemStack gunItem);

    /**
     * 卸载所有弹药
     */
    void unloadAmmo(LivingEntity livingEntity, ItemStack gunItem);

    /**
     * 是否使用虚拟备弹而不是背包物品
     */
    boolean useDummyAmmo(ItemStack gunItem);
    int getDummyAmmoCount(ItemStack gunItem);
    void addDummyAmmoCount(ItemStack gunItem, int amount);
    void setDummyAmmoCount(ItemStack gunItem, int amount);
    boolean hasDummyAmmoLimit(ItemStack gunItem);
    int getDummyAmmoLimit(ItemStack gunItem);
    void setDummyAmmoLimit(ItemStack gunItem, int max);

    /**
     * 是否直读背包备弹
     */
    boolean useInventoryAmmo(ItemStack gunItem);
    /**
     * 检查背包是否有备弹，无关是否为直读模式
     */
    boolean hasInventoryAmmo(ItemStack gunItem);
    int getInventoryAmmoCount(LivingEntity livingEntity, ItemStack gunItem);

    /**
     * 获取当前枪械弹匣弹药数
     */
    int getMagAmmoCount(ItemStack gunItem);
    void setMagAmmoCount(ItemStack gunItem, int count);
    int consumeMagAmmo(ItemStack gunItem);

    /**
     * 消耗枪管里的子弹
     */
    default boolean hasBarrelAmmo(ItemStack gunItem) {
        return getBarrelAmmoCount(gunItem) > 0;
    }
    int getBarrelAmmoCount(ItemStack gunItem);
    void setBarrelAmmoCount(ItemStack gunItem, int amount);

    // --------Deprecated--------

    @Deprecated default int reduceCurrentAmmoCount(ItemStack gunItem) {
        return consumeMagAmmo(gunItem);
    }

    @Deprecated default void dropAllAmmo(Player player, ItemStack gunItem) {
        unloadAmmo(player, gunItem);
    }

    @Deprecated default boolean hasMaxDummyAmmo(ItemStack gunItem) {
        return hasDummyAmmoLimit(gunItem);
    }
    @Deprecated default int getMaxDummyAmmoAmount(ItemStack gunItem) {
        return getDummyAmmoLimit(gunItem);
    }
    @Deprecated default void setMaxDummyAmmoAmount(ItemStack gunItem, int max) {
        setDummyAmmoLimit(gunItem, max);
    }

    @Deprecated default int getCurrentAmmoCount(ItemStack gunItem) {
        return getMagAmmoCount(gunItem);
    }
    @Deprecated default void setCurrentAmmoCount(ItemStack gunItem, int ammoCount) {
        setMagAmmoCount(gunItem, ammoCount);
    }

    @Deprecated default boolean hasBulletInBarrel(ItemStack gunItem) {
        return hasBarrelAmmo(gunItem);
    }
    @Deprecated default void setBulletInBarrel(ItemStack gunItem, boolean value) {
        setBarrelAmmoCount(gunItem, value ? 1 : 0);
    }
}
