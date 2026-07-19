/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;

/*
文档译名: 枪械动作 (XiaoColorful译); 否决译名: 枪械操作, 枪械行为
 */
public interface IGunActionRuntime {

    /**
     * 开始拉栓时调用，返回 bolt 状态
     * @return bolt 状态。ture 代表开始 bolt，false 则代表不开始。
     */
    boolean startBolt(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);
    /**
     * 拉栓 tick 时调用，返回是否仍在 bolt 状态
     * @return 是否仍在 bolt 状态
     */
    boolean tickBolt(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);

    /**
     * 换弹前的检查，完成如下检查：枪内弹药是否已经填满？玩家背包是否有可用弹药？是否为背包直读？
     * @param gunItem 枪械物品
     * @param livingShooter 准备换弹的实体
     * @return 是否满足换弹条件
     */
    boolean canReload(ItemStack gunItem, LivingEntity livingShooter);
    /**
     * 开始换弹时调用
     */
    boolean startReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);
    /**
     * 换弹时每个 tick 调用
     * @return 如果返回的类型是 {@link ReloadState.StateType#NOT_RELOADING} 则下一个 tick 不再继续调用
     */
    ReloadState tickReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);
    /**
     * 尝试打断换弹时调用
     */
    void interruptReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);

    /**
     * 切换开火模式时调用
     */
    void switchFireMode(ShooterProperty shooterProperty, ItemStack gunItem);
}
