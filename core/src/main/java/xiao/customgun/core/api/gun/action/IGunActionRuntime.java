/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.event.shooter.ShooterReloadEvent;
import xiao.customgun.core.api.item.IGun;

/*
文档译名: 枪械动作 (XiaoColorful译); 否决译名: 枪械操作, 枪械行为
 */
public interface IGunActionRuntime {

    /**
     * 开始拉栓时调用，返回 bolt 状态
     * @return 是否开始 bolt
     */
    boolean startBolt(ShooterProperty shooterProperty,
                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                      ILivingShooter iLivingShooter, LivingEntity livingShooter);
    /**
     * 拉栓 tick 时调用，返回是否仍在 bolt 状态
     * @return 是否仍在 bolt 状态
     */
    boolean tickBolt(ShooterProperty shooterProperty,
                     @NotNull IGun iGun, @NotNull ItemStack gunItem,
                     ILivingShooter iLivingShooter, LivingEntity livingShooter);

    /**
     * {@link IGunActionRuntime#startReload}包含该检查，不触发{@link ShooterReloadEvent}
     * <br>
     * 换弹前的检查，完成如下检查：枪内弹药是否已经填满？玩家背包是否有可用弹药？是否为背包直读？
     * @return 是否满足换弹条件
     */
    boolean canReload(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                      ILivingShooter iLivingShooter, LivingEntity livingShooter);
    /**
     * 调用时会执行{@link IGunActionRuntime#canReload}检查，检查通过后触发{@link ShooterReloadEvent}
     * <br>
     * 开始换弹时调用
     */
    boolean startReload(ShooterProperty shooterProperty,
                        @NotNull IGun iGun, @NotNull ItemStack gunItem,
                        ILivingShooter iLivingShooter, LivingEntity livingShooter);
    /**
     * 换弹时每个 tick 调用
     * @return 如果返回的类型是 {@link ReloadState.StateType#NOT_RELOADING} 则下一个 tick 不再继续调用
     */
    ReloadState tickReload(ShooterProperty shooterProperty,
                           @NotNull IGun iGun, @NotNull ItemStack gunItem,
                           ILivingShooter iLivingShooter, LivingEntity livingShooter);
    /**
     * 尝试打断换弹时调用
     */
    void interruptReload(ShooterProperty shooterProperty,
                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                         ILivingShooter iLivingShooter, LivingEntity livingShooter);

    /**
     * 切换开火模式时调用
     */
    void switchFireMode(ShooterProperty shooterProperty,
                        @NotNull IGun iGun, @NotNull ItemStack gunItem);
}
