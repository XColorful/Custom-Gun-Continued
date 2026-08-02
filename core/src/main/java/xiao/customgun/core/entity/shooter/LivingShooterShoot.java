/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.gun.attack.IGunAttackRuntime;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.*;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.config.SyncConfig;
import xiao.customgun.core.network.message.ServerMessageSyncBaseTimestamp;
import xiao.customgun.core.network.message.event.ServerMessageGunShoot;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._ChargingData;
import xiao.customgun.core.resource.data.data.gun._FireModeAdjustData;
import xiao.customgun.core.resource.data.data.gun._HeatData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

import java.util.Map;
import java.util.function.Supplier;

public final class LivingShooterShoot extends LivingShooterAspect {

    private final LivingShooterDraw draw;

    public LivingShooterShoot(LivingEntity livingShooter, ShooterProperty shooterProperty,
                              LivingShooterDraw draw) {
        super(livingShooter, shooterProperty);
        this.draw = draw;
    }

    /**
     * 已经在{@link ILivingShooter#cgc$shoot}提供默认重载实现
     */
    @Deprecated(forRemoval = true)
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw,
                             long timestamp) {
        return shoot(pitch, yaw,
                timestamp,
                0f);
    }
    /**
     * @param hasChargeContext 这个字段始终为 true，没有意义，只要有chargeData就始终检查
     */
    @Deprecated(forRemoval = true)
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw,
                             long timestamp,
                             float chargeProgress, boolean hasChargeContext) {
        return shoot(pitch, yaw,
                timestamp,
                chargeProgress);
    }
    /**
     * 执行一次射击
     */
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw,
                              long timestamp,
                              float chargeProgress) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return ShootResult.NOT_DRAW;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return ShootResult.NOT_GUN;

        if ( // 2.2
                preCheckError(timestamp) != null
        ) return ShootResult.PRE_CHECK_ERROR;

        // --------TODO
//        int consumedAmmo = iGun.consumeAmmoOnce(this.livingShooter, currentGunItem);
//        if (consumedAmmo <= 0) {
//            return ShootResult.NO_AMMO;
//        }
        // 消耗子弹
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        // --------

        { // 3. IGunRuntime操作结果 -> Shooter状态
            /**
             * {@link IGunAttackRuntime#shooterFire}的默认实现为{@link IGunAttackRuntime#shooterFire}
             */
            @NotNull IGunAttackRuntime.ShooterFireResult shooterFireResult = iGun.shooterFire(this.shooterProperty, iGun, gunItem, iLivingShooter, this.livingShooter, pitch, yaw, chargeProgress);
            if (!shooterFireResult.isSuccess()) {
                return ShootResult.UNKNOWN_FAIL;
            }
            this.shooterProperty.lastShootTimestamp = this.shooterProperty.shootTimestamp;
            this.shooterProperty.shootTimestamp = timestamp;
            this.shooterProperty.heatTimestamp = System.currentTimeMillis();
            this.shooterProperty.chargeProgress = validateChargeProgress(iGun, gunItem, chargeProgress);
            // 发包通知客户端
            SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                    new ServerMessageGunShoot(this.livingShooter.getId(), gunItem));
        }

        /**
         * {@link IGunAttackRuntime#gunFire}的默认实现为{@link IGunAttackRuntime#gunFire}
         */
        @NotNull IGunAttackRuntime.GunFireResult gunFireResult = iGun.gunFire(this.shooterProperty, iGun, gunItem, iLivingShooter, this.livingShooter, pitch, yaw);
        if (!gunFireResult.isSuccess()) {
            return ShootResult.UNKNOWN_FAIL;
        }
        return ShootResult.SUCCESS;
    }
    @Nullable
    private ShootResult preCheckError(long timestamp) {
        if (SyncConfig.SERVER_SHOOT_COOLDOWN_V.get()) {
            // 判断射击是否正在冷却
            long coolDown = _getShootCooldown(timestamp);
            if (coolDown < 0) return ShootResult.UNKNOWN_FAIL;
            else if (coolDown > 0) return ShootResult.COOL_DOWN;
        }

        if (SyncConfig.SERVER_SHOOT_NETWORK_V.get()) {
            // 根据 tick time 和 允许的网络延迟波动 计算 时间戳的接受窗口
            MinecraftServer server = ((ServerLevel) this.livingShooter.level()).getServer();
            double tickTime = Math.max(server.tickTimes[server.getTickCount() % 100] * 1.0E-6D, 50);
            long alpha = System.currentTimeMillis() - this.shooterProperty.baseTimestamp - timestamp;
            if (alpha < -NETWORK_DELAY_MS || alpha > NETWORK_DELAY_MS + tickTime * 2) { // 允许 +- 300ms 的网络波动、窗口下限再扩大 2 个 tick time 时间(最坏情况射击会延迟2个 tick)
                if (this.livingShooter instanceof ServerPlayer player) {
                    SendUtils.sendMessageToPlayer(player, new ServerMessageSyncBaseTimestamp());
                }
                return ShootResult.NETWORK_FAIL;
            }
        }

        // 检查是否正在换弹
        if (this.shooterProperty.reloadStateType.isReloading()) {
            return ShootResult.IS_RELOADING;
        }
        // 检查是否在切枪
        if (draw.getDrawCooldown() > 0) {
            return ShootResult.IS_DRAWING;
        }
        // 检查是否在拉栓
        if (this.shooterProperty.isBolting) {
            return ShootResult.IS_BOLTING;
        }
        // 检查是否在奔跑
        if (this.shooterProperty.sprintTimeS > 0) {
            return ShootResult.IS_SPRINTING;
        }

        return null;
    }
    private float validateChargeProgress(IGun iGun, ItemStack gunItem,
                                         float chargeProgress) {
        @Nullable Map<FireModeType, _ChargingData> chargingDataMap = GunDataAccessor._getChargingData(iGun, gunItem);
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        @Nullable _ChargingData chargingData = chargingDataMap != null ? chargingDataMap.get(fireModeType) : null;

        if (!true || !Float.isFinite(chargeProgress)) {
            return 0f;
        }
        if (chargingData == null) {
            return 0f;
        }
        return Math.max(0f, Math.min(chargeProgress, chargingData.getMaxCharge()));
    }

    /**
     * 以当前时间戳查询射击冷却。返回值一般不会超过枪械的射击间隔
     * @return 射击冷却
     */
    public long getShootCooldown() {
        return _getShootCooldown(System.currentTimeMillis() - this.shooterProperty.baseTimestamp);
    }
    /**
     * 查询指定的 timestamp 下的射击冷却。根据情况返回值可能超过枪械的射击间隔。
     * @param timestamp 指定 timestamp，是偏移时间戳（基于base timestamp 的相对时间戳）
     * @return 射击冷却
     */
    private long _getShootCooldown(long timestamp) {
        if (this.shooterProperty.currentGunItem == null) return 0;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return 0;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return -1;

        GunData gunData = gunIndexInstance.getGunData();
        long interval = timestamp - this.shooterProperty.shootTimestamp;

        FireModeType fireModeType = iGun.getFireModeType(currentGunItem);
        if (fireModeType == FireModeType.BURST) {
            long coolDown = (long) (gunData.getBurstData().getShootIntervalSeconds() * 1000f) - interval;
            // 给 5 ms 的窗口时间，以平衡延迟
            coolDown = coolDown - WINDOW_TIME_MS;
            return Math.max(coolDown, 0L);
        } else {
            long shootInterval = _getShootInterval(this.livingShooter, gunData, fireModeType, iGun, currentGunItem);

            long coolDown = shootInterval - interval;
            // 给 5 ms 的窗口时间，以平衡延迟
            coolDown = coolDown - 5;
            return Math.max(coolDown, 0L);
        }
    }

    // TODO 把原模组GunData里的getShootInterval找个位置
    public static long _getShootInterval(LivingEntity livingShooter,
                                         GunData gunData, FireModeType fireModeType, IGun iGun, ItemStack gunItem) {

        // ----TODO 把原模组GunData里的getRoundsPerMinute找个位置
        int rpm = gunData.getRpm();
        _FireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData().get(fireModeType);
        if (fireModeAdjustData != null) {
            rpm += fireModeAdjustData.getRpm();
        }
        if (rpm <= 0) rpm = 300;
        // ----

        ShooterGunModifierCache shooterGunModifierCache = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$getGunModifierCache();
        if (shooterGunModifierCache != null) {
            // TODO GunPropertyCache
        }
        _HeatData heatData = gunData.getHeatData();
        if (heatData != null) {
            rpm = (int) (rpm * iGun.lerpRPM(gunItem));
        }

        return 60_000L / rpm;
    }
}
