/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.entity.shooter;

import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.api.entity.shooter.ICommonGunOperator;

public interface IClientGunOperator extends ICommonGunOperator {

    // --------行为动作--------

    /**
     * 客户端爬行
     */
    @Override void cgc$prone(boolean isProne);


    // --------枪械操作--------

    /**
     * 执行客户端切枪逻辑
     */
    void cgc$clientDraw(ItemStack lastItem);
    void cgc$resetDraw();

    /**
     * 客户端切换开火模式
     */
    @Override void cgc$switchFireMode();

    /**
     * 客户端瞄准
     */
    @Override void cgc$aim(boolean isAim);

    /**
     * 客户端近战（刺刀）
     */
    @Override void cgc$melee();

    /**
     * 检查玩家能否开火，并执行客户端开火逻辑
     * @return 返回开火的结果
     */
    ShootResult cgc$localShoot();
    /**
     * 尝试充能/尝试开火
     * @return 本次是否应该射击 (充能进度满/不需要充能)
     */
    boolean cgc$chargeAndGetResult(boolean doShoot);

    /**
     * 客户端手动拉栓(换弹)
     */
    @Override void cgc$bolt();

    /**
     * 客户端换弹
     */
    @Override void cgc$reload();

    /**
     * 客户端检视
     */
    void cgc$inspect();

    // --------Deprecated--------

    @Deprecated default boolean cgc$chargeShoot(boolean isCharging) {
        return this.cgc$chargeAndGetResult(isCharging);
    }
}