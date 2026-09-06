/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.IEntityHitboxHistory;
import dev.xcolorful.customgun.core.api.entity.ShootState;
import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IShooterState extends IShooterLatency {

    /**
     * 服务端，该操作者是否受弹药数影响
     *
     * @return 如果为 false，那么开火时不会检查弹药，无论是玩家背包内还是枪械内的
     */
    boolean cgc$needCheckAmmo();

    /**
     * 服务端，开火是否消耗弹药
     *
     * @return 如果为 false，那么开火不会消耗枪械弹药
     */
    boolean cgc$consumesAmmoOrNot();

    /**
     * 根据情况返回玩家应当处于的冲刺状态，在玩家切换冲刺状态的时候调用。
     * 这里的逻辑应该严格与客户端端对应，如果不对应，会出现客户端表现和服务端不符的情况。
     * （例如客户端的视觉效果是玩家在冲刺，而服务端玩家实际上没有冲刺）
     * TODO
     */
    boolean cgc$getProcessedSprintStatus(boolean sprint);

    /**
     * @return 当前射击状态，可用于不准确度计算
     */
    ShootState cgc$getShootState();

    /**
     * @param livingShooter 射手生物
     * @param latencyLerp 是否考虑 {@link IShooterLatency} 和 {@link IEntityHitboxHistory} 进行插值
     * @return 射手生物眼部位置
     */
    default @NotNull Vec3 cgc$getShooterEyePos(@Nullable LivingEntity livingShooter, boolean latencyLerp) {
        return getShooterEyePos(livingShooter, latencyLerp);
    }
    /**
     * {@link #cgc$getShooterEyePos} 的默认实现
     * <ul>
     *     碰撞箱历史插值:
     *     <li>取{@link IShooterLatency#cgc$getShooterLatencyMs()}延迟</li>
     *     <li>取{@link IEntityHitboxHistory#cgc$getHistoryHitbox}碰撞箱历史</li>
     *     <li>根据小数 tick 在前后两个历史碰撞箱的眼部高度中心点间进行插值</li>
     * </ul>
     */
    static @NotNull Vec3 getShooterEyePos(@Nullable LivingEntity livingShooter, boolean latencyLerp) {
        if (livingShooter == null) return Vec3.ZERO;

        double spawnPosX;
        double spawnPosY;
        double spawnPosZ;
        if (latencyLerp) {
            // 进行插值
            @Nullable IEntityHitboxHistory entityHitboxHistory = IEntityHitboxHistoryGetter.cgc$fromEntity(livingShooter);
            if (entityHitboxHistory != null) {
                // 查碰撞箱历史
                IShooterLatency iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter);
                int shooterLatencyMs = iLivingShooter.cgc$getShooterLatencyMs();

                // 将毫秒延迟转换为游戏刻
                double latencyTicks = shooterLatencyMs / 50.0; // 1tick 50ms
                int tickBefore = (int) latencyTicks;
                double partialTick = latencyTicks - tickBefore;

                // 获取延迟前对应时刻及更早一刻的碰撞箱
                @Nullable AABB current = entityHitboxHistory.cgc$getHistoryHitbox(tickBefore);
                @Nullable AABB previous = entityHitboxHistory.cgc$getHistoryHitbox(tickBefore + 1);

                if (current != null && previous != null) {
                    // 以碰撞箱中心及眼部高度作为计算基准
                    double currentX = (current.minX + current.maxX) / 2.0;
                    double currentY = current.minY + livingShooter.getEyeHeight();
                    double currentZ = (current.minZ + current.maxZ) / 2.0;

                    double previousX = (previous.minX + previous.maxX) / 2.0;
                    double previousY = previous.minY + livingShooter.getEyeHeight();
                    double previousZ = (previous.minZ + previous.maxZ) / 2.0;

                    // 在前后两个时刻的眼部位置间进行线性插值
                    spawnPosX = Mth.lerp(partialTick, currentX, previousX);
                    spawnPosY = Mth.lerp(partialTick, currentY, previousY);
                    spawnPosZ = Mth.lerp(partialTick, currentZ, previousZ);
                    return new Vec3(spawnPosX, spawnPosY, spawnPosZ);
                }
            }

            // 回退到普通插值
            spawnPosX = Mth.lerp(0.5, livingShooter.xOld, livingShooter.getX());
            spawnPosY = Mth.lerp(0.5, livingShooter.yOld, livingShooter.getY()) + livingShooter.getEyeHeight();
            spawnPosZ = Mth.lerp(0.5, livingShooter.zOld, livingShooter.getZ());
            return new Vec3(spawnPosX, spawnPosY, spawnPosZ);
        } else {
            // 不插值
            spawnPosX = livingShooter.getX();
            spawnPosY = livingShooter.getY() + livingShooter.getEyeHeight();
            spawnPosZ = livingShooter.getZ();
            return new Vec3(spawnPosX, spawnPosY, spawnPosZ);
        }
    }
}
