/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.inventory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.gun.inventory.GunInventoryManager;

public interface IGunInventoryRuntime {

    /**
     * 尝试将枪内的弹药取回，其中:
     * <ul>
     *     <li>不保证全部取回(如燃料类型)</li>
     *     <li>不保证能卸载供弹</li>
     * </ul>
     * <br>
     * 模组内置的默认实现(见{@link GunInventoryManager#retrieveAmmoFromGun})如下:
     * <ul>
     *     <li>先退至背包，其次丢到地上</li>
     *     <li>不会退枪膛内的弹药</li>
     * </ul>
     * 可以用于:
     * <ul>
     *     <li>明确需要将枪内子弹取回</li>
     * </ul>
     * @param gunItem 枪械物品
     * @param livingShooter 准备退弹的实体
     */
    void retrieveAmmoFromGun(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                             ILivingShooter iLivingShooter, LivingEntity livingShooter);

    /**
     * 枪械寻弹和扣除背包弹药逻辑
     * @param inventoryCapability 目标实体的背包
     * @param gunItem 枪械物品
     * @param requiredAmmoCount 需要的弹药 (物品) 数量
     * @return 扣除的弹药 (物品) 数量
     */
    int findAndExtractInventoryAmmo(@NotNull IInventoryCapability inventoryCapability,
                                    @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                    int requiredAmmoCount);

    /**
     * 扣除虚拟弹药逻辑，该方法具有通用的实现，放在此处
     * @param gunItem 枪械物品
     * @param requiredAmmoCount 需要的弹药(物品)数量
     * @return 扣除的弹药(物品)数量
     */
    int findAndExtractDummyAmmo(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                int requiredAmmoCount);

    // --------Deprecated--------

    @Deprecated default void dropAllAmmo(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                             ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        retrieveAmmoFromGun(iGun, gunItem, iLivingShooter, livingShooter);
    }
}
