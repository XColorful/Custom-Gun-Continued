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
import org.jetbrains.annotations.ApiStatus;
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
import xiao.customgun.core.resource.data.data.gun._FireModeAdjustData;
import xiao.customgun.core.resource.data.data.gun._HeatData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

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
                             long clientFromBaseToCurrentTimeMs) {
        return shoot(pitch, yaw,
                clientFromBaseToCurrentTimeMs,
                0f);
    }
    /**
     * @param hasChargeContext 这个字段始终为 true，没有意义，只要有chargeData就始终检查
     */
    @Deprecated(forRemoval = true)
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw,
                             long clientFromBaseToCurrentTimeMs,
                             float chargeProgress, boolean hasChargeContext) {
        return shoot(pitch, yaw,
                clientFromBaseToCurrentTimeMs,
                chargeProgress);
    }
    /**
     * 执行一次射击
     */
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw,
                              long clientFromBaseToCurrentTimeMs,
                              float chargeProgress) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return ShootResult.NOT_DRAW;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return ShootResult.NOT_GUN;

        final long currentTimeMillis = System.currentTimeMillis();
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        if ( // 2.2 检查状态
                // 禁止射击的状态
                _shouldForceDisableShoot()
                // 近战冷却
                || iLivingShooter.cgc$_getMeleeCooldownMs(currentTimeMillis) > 0
                // 服务端射击冷却
                || SyncConfig.SERVER_SHOOT_COOLDOWN_V.get() && isInServerShootCooldown(currentTimeMillis, clientFromBaseToCurrentTimeMs)
        ) return ShootResult.UNKNOWN_FAIL;

        // --------TODO
//        int consumedAmmo = iGun.consumeAmmoOnce(this.livingShooter, currentGunItem);
//        if (consumedAmmo <= 0) {
//            return ShootResult.NO_AMMO;
//        }
        // 消耗子弹
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
            this.shooterProperty.shootTimestamp = clientFromBaseToCurrentTimeMs;
            this.shooterProperty.heatTimestamp = currentTimeMillis;
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

    @ApiStatus.Internal
    public boolean _shouldForceDisableShoot() {
        if (isIllegalShootState(this.livingShooter)) return true;

        if ( // 2.2 检查状态
                // 正在换弹
                this.shooterProperty.reloadStateType.isReloading()
                // 正在切枪
                || draw.getDrawCooldown() > 0
                // 正在拉栓
                || this.shooterProperty.isBolting
                // 正在疾跑
                || this.shooterProperty.sprintTimeS > 0
        ) return true;

        return false;
    }
    @ApiStatus.Internal
    public boolean isInServerShootCooldown(long currentTimeMillis, long clientFromBaseToCurrentTimeMs) {
        if (this.shooterProperty.currentGunItem == null) return false;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;

        return this.isInServerShootCooldown(iGun, gunItem, currentTimeMillis, clientFromBaseToCurrentTimeMs);
    }
    private boolean isInServerShootCooldown(IGun iGun, ItemStack gunItem,
                                            long currentTimeMillis, long clientFromBaseToCurrentTimeMs) {
        // 判断射击是否正在冷却
        long coolDown = _getShootCooldown(iGun, gunItem, clientFromBaseToCurrentTimeMs);
        if (coolDown > 0) return true;

        // 根据 tick time 和 允许的网络延迟波动 计算 时间戳的接受窗口
        MinecraftServer server = ((ServerLevel) this.livingShooter.level()).getServer();
        double tickTime = Math.max(server.tickTimes[server.getTickCount() % 100] * 1.0E-6D, 50);
        long alpha = currentTimeMillis - this.shooterProperty.baseTimestamp - clientFromBaseToCurrentTimeMs;
        if (alpha < -NETWORK_DELAY_MS || alpha > NETWORK_DELAY_MS + tickTime * 2) { // 允许 +- 300ms 的网络波动、窗口下限再扩大 2 个 tick time 时间(最坏情况射击会延迟2个 tick)
            if (this.livingShooter instanceof ServerPlayer player) {
                SendUtils.sendMessageToPlayer(player, new ServerMessageSyncBaseTimestamp());
            }
            return true;
        }

        return false;
    }
    @ApiStatus.Internal
    public static boolean isIllegalShootState(LivingEntity livingShooter) {
        return false;
    }

    /**
     * 以当前时间戳查询射击冷却。返回值一般不会超过枪械的射击间隔
     * @return 射击冷却
     */
    public long getShootCooldown() {
        if (this.shooterProperty.currentGunItem == null) return 0;

        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        return _getShootCooldown(iGun, gunItem, System.currentTimeMillis() - this.shooterProperty.baseTimestamp);
    }
    /**
     * 查询指定的 timestamp 下的射击冷却。根据情况返回值可能超过枪械的射击间隔。
     * @param clientFromBaseToCurrentTimeMs 指定 timestamp，是偏移时间戳（基于base timestamp 的相对时间戳）
     * @return 射击冷却
     */
    private long _getShootCooldown(IGun iGun, ItemStack gunItem,
                                   long clientFromBaseToCurrentTimeMs) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();

        long interval = clientFromBaseToCurrentTimeMs - this.shooterProperty.shootTimestamp;
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        if (fireModeType == FireModeType.BURST) {
            long coolDown = (long) (gunData.getBurstData().getShootIntervalSeconds() * 1000f) - interval;
            // 给 5 ms 的窗口时间，以平衡延迟
            coolDown = coolDown - WINDOW_TIME_MS;
            return Math.max(coolDown, 0L);
        } else {
            long shootInterval = _getShootInterval(this.livingShooter, gunData, fireModeType, iGun, gunItem);

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
