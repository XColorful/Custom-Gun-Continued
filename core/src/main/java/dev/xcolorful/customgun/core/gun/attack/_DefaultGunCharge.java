/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.attack;

import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.resource.data.data.gun._ChargingData;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.xcolorful.customgun.core.entity.shooter.LivingShooterAspect.CHARGE_PROGRESS_TOLERANCE;
import static dev.xcolorful.customgun.core.entity.shooter.LivingShooterAspect.CHARGE_TICK_TOLERANCE;

public class _DefaultGunCharge {

    /**
     * 服务端不追踪扳机按住状态，只拒绝超过"客户端一直按住蓄力"时理论可达到的最大进度
     * @return 本次射击是否可接受
     */
    @ApiStatus.Internal
    protected static boolean isChargeProgressAcceptable(@Nullable ShooterProperty shooterProperty,
                                                        @Nullable _ChargingData chargingData,
                                                        float clientChargeProgress) {
        // 只对有charge数据的才启用蓄力检查
        if (chargingData == null) return true;

        if (!Float.isFinite(clientChargeProgress)) return false; // 客户端瞎传数值
        if (clientChargeProgress < -CHARGE_PROGRESS_TOLERANCE) return false;

        // 达到最小蓄力目标
        float progressGoal = Math.min(chargingData.getFireThreshold(), chargingData.getMaxCharge());
        if (clientChargeProgress + CHARGE_PROGRESS_TOLERANCE < progressGoal) return false;

        // 检查理论最大进度
        if (shooterProperty != null && clientChargeProgress > _maxPossibleChargeProgress(shooterProperty, chargingData) + CHARGE_PROGRESS_TOLERANCE) {
            return false;
        }

        return true;
    }
    private static float _maxPossibleChargeProgress(@NotNull ShooterProperty shooterProperty, _ChargingData chargingData) {
        // 预留少量 tick 余量，用于容忍网络抖动和客户端/服务端调度偏差
        float elapsedTicks = Math.max(_getChargeElapsedMs(shooterProperty) / 50f, 0f) + CHARGE_TICK_TOLERANCE;

        float startProgress = _getChargeProgressAfterLastFire(shooterProperty, chargingData);
        float maxProgress = startProgress + elapsedTicks * Math.max(chargingData.getChargePerTick(), 0f);

        return Math.min(maxProgress, chargingData.getMaxCharge());
    }
    private static long _getChargeElapsedMs(@NotNull ShooterProperty shooterProperty) {
        if (shooterProperty.shootTimestamp >= 0) {
            // 上次开枪到现在的时间
            long startTimestamp = shooterProperty.baseTimestamp + shooterProperty.shootTimestamp;
            return System.currentTimeMillis() - startTimestamp;
        } else if (shooterProperty.drawFinishTimestamp >= 0) {
            // 切完枪到现在的时间
            return System.currentTimeMillis() - shooterProperty.drawFinishTimestamp;
        } else return 0;
    }
    private static float _getChargeProgressAfterLastFire(@NotNull ShooterProperty shooterProperty, _ChargingData chargeData) {
        if (
                shooterProperty.shootTimestamp < 0 // 没开过枪
                || chargeData.getChargeType().resetChargeAfterShoot() // 重置进度的一定重新开始计算
        ) return 0f;
        else return Math.max(0f, shooterProperty.chargeProgress - chargeData.getRecoverByFire());
    }

    protected static float clampChargeProgress(@Nullable ShooterProperty shooterProperty,
                                               @Nullable _ChargingData chargingData,
                                               float finalChargeProgress) {
        // 只对有charge数据的才启用蓄力检查
        if (chargingData == null) return finalChargeProgress;

        if (!Float.isFinite(finalChargeProgress)) return 0; // 客户端瞎传数值

        return Mth.clamp(finalChargeProgress, 0, chargingData.getMaxCharge());
    }
}
