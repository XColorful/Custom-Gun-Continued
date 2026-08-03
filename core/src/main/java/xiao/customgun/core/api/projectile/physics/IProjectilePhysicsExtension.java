/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.projectile.physics;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

public interface IProjectilePhysicsExtension {

    /**
     * 根据射击者旋转角度和散布偏移设置投射物初始运动状态
     * <ul>
     *     实现应完成以下处理：
     *     <li>根据散布偏移和射击方向计算投射物初始速度</li>
     *     <li>根据初始速度同步投射物朝向</li>
     *     <li>初始化投射物旋转缓存，保证渲染插值正常</li>
     *     <li>继承射击者自身移动速度</li>
     * </ul>
     *
     * @param livingShooter 发射投射物的实体
     * @param projectile    发射的投射物
     * @param xRot          垂直旋转角度
     * @param yRot          水平旋转角度
     * @param yOffset       垂直方向偏移量
     * @param pow           投射物初速度
     * @param spreadOffset  投射物散布偏移
     */
    void shootFromRotation(Entity livingShooter, @NotNull Projectile projectile,
                           float xRot, float yRot, float yOffset, float pow,
                           Vec2 spreadOffset);
}
