/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter;

import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ShootResult;

import java.util.function.Supplier;

public interface IGunOperator extends ICommonGunOperator {

    // --------行为动作--------

    /**
     * 应用趴下 (服务端)
     */
    @Override void cgc$prone(boolean isProne);

    // --------枪械操作--------

    /**
     * 切枪逻辑 (服务端)
     */
    void cgc$draw(Supplier<ItemStack> itemStackSupplier);

    /**
     * 切换开火模式 (服务端)
     */
    @Override void cgc$switchFireMode();

    /**
     * 应用瞄准 (服务端)
     */
    @Override void cgc$aim(boolean isAim);
    /**
     * 倍镜缩放 (服务端)
     */
    void cgc$zoom();

    /**
     * 刺刀/近战 (服务端)
     */
    @Override void cgc$melee();

    /**
     * 从实体的位置，向指定的方向开枪
     * @param pitch 开火方向的俯仰角 (xRot)
     * @param yaw   开火方向的偏航角 (yRot)
     * @param timestamp 计算冷却的时候使用的时间戳，为偏移时间戳（相对于 base timestamp 的时间戳）
     * @return 本次射击的结果
     */
    ShootResult cgc$shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress);
    ShootResult cgc$shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp);
    ShootResult cgc$shoot(Supplier<Float> pitch, Supplier<Float> yaw);

    /**
     * 拉栓 (服务端)
     */
    @Override void cgc$bolt();

    /**
     * 换弹 (服务端)
     */
    @Override void cgc$reload();
    /**
     * 取消换弹 (服务端)
     */
    void cgc$cancelReload();
}
