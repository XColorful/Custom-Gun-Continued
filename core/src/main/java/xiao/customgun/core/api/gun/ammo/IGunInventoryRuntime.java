/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.ammo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;

public interface IGunInventoryRuntime {

    /**
     * 将枪内的弹药全部退至背包（如果背包满了会丢到地上）。不会退枪膛内的弹药。
     * 目前，仅更换弹匣配件时调用。
     * @param gunItem 枪械物品
     * @param livingShooter 准备退弹的实体
     */
    void dropAllAmmo(ItemStack gunItem, LivingEntity livingShooter);

    /**
     * 枪械寻弹和扣除背包弹药逻辑
     * @param inventoryCapability 目标实体的背包
     * @param gunItem 枪械物品
     * @param needAmmoCount 需要的弹药 (物品) 数量
     * @return 寻找到的弹药 (物品) 数量
     */
    int findAndExtractInventoryAmmo(IInventoryCapability inventoryCapability, ItemStack gunItem, int needAmmoCount);

    /**
     * 扣除虚拟弹药逻辑，该方法具有通用的实现，放在此处
     * @param gunItem 枪械物品
     * @param needAmmoCount 需要的弹药(物品)数量
     * @return 找到的弹药(物品)数量
     */
    int findAndExtractDummyAmmo(ItemStack gunItem, int needAmmoCount);
}
