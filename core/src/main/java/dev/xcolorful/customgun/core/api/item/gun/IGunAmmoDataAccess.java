/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun;

import dev.xcolorful.customgun.core.api.gun.inventory.IGunInventoryRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IGunAmmoDataAccess {

    /**
     * 子弹类型是否匹配
     */
    boolean isMatchedAmmo(ItemStack gunItem, ItemStack ammoItem);
    /**
     * @param gunItem 枪械
     * @param ammoItem 子弹
     * @return 该子弹中可用于枪械的数量
     */
    int consumableAmmoCount(ItemStack gunItem, ItemStack ammoItem);

    /**
     * 为一次射击消耗一次子弹
     * <ul>
     *     根据{@link BoltType#useBarrelAmmo()}和{@link BoltType#autoBoltBarrelAmmo()}区分：
     *     <li>{@link BoltType#MANUAL_ACTION}只消耗枪管里的子弹 ({@link IGunAmmoDataAccess#hasBarrelAmmo})</li>
     *     <li>{@link BoltType#CLOSED_BOLT}只消耗枪管里的子弹，无论枪管是否有子弹，射击后都会自动上膛</li>
     *     <li>{@link BoltType#OPEN_BOLT}只消耗弹匣子弹 ({@link IGunAmmoDataAccess#getMagAmmoCount})，不消耗枪管子弹</li>
     *     <li>无论是否实际消耗了子弹，返回正数即代表“应消耗了子弹”</li>
     * </ul>
     * @return 消耗的子弹数，返回{@code 0}则无法消耗子弹
     */
    int consumeAmmoOnce(@Nullable LivingEntity livingEntity, ItemStack gunItem, BoltType boltType);
    /**
     * {@link IGunAmmoDataAccess#consumeAmmoOnce(LivingEntity, ItemStack, BoltType)}的便利方法
     */
    int consumeAmmoOnce(@Nullable LivingEntity livingEntity, ItemStack gunItem);

    /**
     * 尝试卸载供弹(如弹匣)，其中:
     * <ul>
     *     <li>不指定退弹方式</li>
     *     <li>不保证能退回物品(例如子弹数据缺失)</li>
     *     <li>不保证子弹退回背包</li>
     * </ul>
     * 可以用于:
     * <ul>
     *     <li>更换弹匣前的清理</li>
     * </ul>
     * 如果明确要"取回"供弹，应使用{@link IGunInventoryRuntime#retrieveAmmoFromGun}，且该草走不保证能卸载供弹
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
    boolean hasInventoryAmmo(LivingEntity livingEntity, ItemStack gunItem);
    int getInventoryAmmoCount(LivingEntity livingEntity, ItemStack gunItem);

    /**
     * 获取当前枪械弹匣弹药数
     */
    int getMagAmmoCount(ItemStack gunItem);
    int getMagAmmoCountWithBarrel(ItemStack gunItem, BoltType boltType);
    void setMagAmmoCount(ItemStack gunItem, int count);
    /**
     * 消耗一次弹匣子弹
     * @return 返回消耗的子弹数，为0则不消耗子弹
     */
    int consumeMagAmmoOnce(ItemStack gunItem);

    /**
     * 查询当前配件下弹匣大小
     */
    int getMagAmmoLimit(ItemStack gunItem);

    /**
     * 枪管里是否有子弹
     */
    default boolean hasBarrelAmmo(ItemStack gunItem) {
        return getBarrelAmmoCount(gunItem) > 0;
    }
    /**
     * 枪管里的子弹数量
     */
    int getBarrelAmmoCount(ItemStack gunItem);
    void setBarrelAmmoCount(ItemStack gunItem, int amount);

    /**
     * 执行一次拉栓的子弹上膛
     * @return 消耗的子弹数
     */
    int boltBarrelAmmo(@Nullable LivingEntity livingEntity, ItemStack gunItem, BoltType boltType);
    /**
     * {@link IGunAmmoDataAccess#boltBarrelAmmo(LivingEntity, ItemStack, BoltType)}的便利方法
     */
    int boltBarrelAmmo(@Nullable LivingEntity livingEntity, ItemStack gunItem);

    // --------Deprecated--------

    @Deprecated default int reduceCurrentAmmoCount(ItemStack gunItem) {
        return consumeMagAmmoOnce(gunItem);
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
