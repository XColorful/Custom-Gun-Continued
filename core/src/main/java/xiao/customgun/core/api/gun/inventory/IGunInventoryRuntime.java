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

public interface IGunInventoryRuntime {

    /**
     * 将枪内的弹药全部退至背包（如果背包满了会丢到地上），不会退枪膛内的弹药
     * @param gunItem 枪械物品
     * @param livingShooter 准备退弹的实体
     */
    void dropAllAmmo(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                     ILivingShooter iLivingShooter, LivingEntity livingShooter);

    /**
     * 枪械寻弹和扣除背包弹药逻辑
     * @param inventoryCapability 目标实体的背包
     * @param gunItem 枪械物品
     * @param requiredAmmoCount 需要的弹药 (物品) 数量
     * @return 扣除的弹药 (物品) 数量
     */
    int findAndExtractInventoryAmmo(IInventoryCapability inventoryCapability,
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
}
