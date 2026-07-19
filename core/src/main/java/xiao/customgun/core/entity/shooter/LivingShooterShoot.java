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
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.gun.GunPropertyCache;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterFireEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.ChargeType;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.config.SyncConfig;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.event.EventPoster;
import xiao.customgun.core.network.message.ServerMessageSyncBaseTimestamp;
import xiao.customgun.core.network.message.event.ServerMessageGunShoot;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._ChargingData;
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

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        return shootInternal(pitch, yaw, timestamp, 0f, false);
    }
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
        return shootInternal(pitch, yaw, timestamp, chargeProgress, true);
    }
    private ShootResult shootInternal(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress, boolean hasChargeContext) {
        if (this.shooterProperty.currentGunItem == null) return ShootResult.NOT_DRAW;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return ShootResult.NOT_GUN;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return ShootResult.ID_NOT_EXIST;

        GunData gunData = gunIndexInstance.getGunData();
        FireModeType fireModeType = iGun.getFireModeType(currentGunItem);
        @Nullable _ChargingData chargeData = gunData.getChargingData().get(fireModeType);

        ShootResult errorResult = preCheckError(iGun, currentGunItem,
                gunData, chargeData, timestamp, chargeProgress, hasChargeContext);
        if (errorResult != null) return errorResult;

        // --------TODO
//        int consumedAmmo = iGun.consumeAmmoOnce(this.livingShooter, currentGunItem);
//        if (consumedAmmo <= 0) {
//            return ShootResult.NO_AMMO;
//        }
        // 消耗子弹
        BoltType boltType = gunData.getBoltType();
        boolean useInventoryAmmo = iGun.useInventoryAmmo(currentGunItem); // 是否为背包直读
        boolean hasAmmo = useInventoryAmmo ? iGun.hasInventoryAmmo(this.livingShooter, currentGunItem)
                : iGun.getMagAmmoCountWithBarrel(currentGunItem, boltType) > 0;
        if (!hasAmmo) {
            return ShootResult.NO_AMMO;
        }
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        switch (boltType) {
            case MANUAL_ACTION -> {// 检查膛内子弹
                if (!iGun.hasBarrelAmmo(currentGunItem)) return ShootResult.NEED_BOLT;
            }
            case CLOSED_BOLT -> {// 闭膛的膛内检查逻辑
                if (!iGun.hasBarrelAmmo(currentGunItem)) {
                    if (useInventoryAmmo) this.consumeAmmoFromPlayer(iGun, currentGunItem, iLivingShooter.cgc$needCheckAmmo());
                    else iGun.consumeMagAmmo(currentGunItem);

                    if (PlannedRefactor.ON_SET_BARREL_AMMO) {};
                    iGun.setBarrelAmmoCount(currentGunItem, 1);
                }
            }
        }
        // --------

        if (EventPoster.get().postCustomEvent(new ShooterFireEvent(McLogicalSide.SERVER,
                iLivingShooter, this.livingShooter, iGun, currentGunItem))) {
            return ShootResult.EVENT_CANCELED;
        }
        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunShoot(this.livingShooter.getId(), currentGunItem));

        this.shooterProperty.lastShootTimestamp = this.shooterProperty.shootTimestamp;
        this.shooterProperty.shootTimestamp = timestamp;
        this.shooterProperty.heatTimestamp = System.currentTimeMillis();
        this.shooterProperty.chargeProgress = validateChargeProgress(chargeData, chargeProgress, hasChargeContext);

        // 执行枪械射击逻辑
        iGun.shoot(this.shooterProperty, currentGunItem, this.livingShooter, pitch, yaw);
        return ShootResult.SUCCESS;
    }
    @Nullable
    private ShootResult preCheckError(IGun iGun, ItemStack gunItem,
                                      GunData gunData, _ChargingData chargeData, long timestamp, float chargeProgress, boolean hasChargeContext) {
        if (SyncConfig.SERVER_SHOOT_COOLDOWN_V.get()) {
            // 判断射击是否正在冷却
            long coolDown = getShootCooldown(timestamp);
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

        if (hasChargeContext && !isChargeProgressReasonable(chargeData, chargeProgress)) {
            return ShootResult.UNKNOWN_FAIL;
        }

        // 检查过热锁
        if (iGun.hasHeat(gunItem)) {
            if (iGun.hasOverheatLock(gunItem)) {
                return ShootResult.OVERHEATED;
            }
        }

        return null;
    }
    private float validateChargeProgress(@Nullable _ChargingData chargeData, float chargeProgress, boolean hasChargeContext) {
        if (!hasChargeContext || !Float.isFinite(chargeProgress)) {
            return 0f;
        }
        if (chargeData == null) {
            return 0f;
        }
        return Math.max(0f, Math.min(chargeProgress, chargeData.getMaxCharge()));
    }

    /**
     * 简单校验: 服务端不追踪扳机按住状态 -> 只拒绝超过"客户端一直按住蓄力"时理论可达到的最大进度
     */
    private boolean isChargeProgressReasonable(@Nullable _ChargingData chargeData, float chargeProgress) {
        if (!Float.isFinite(chargeProgress)) return false;
        if (chargeData == null) return Math.abs(chargeProgress) <= CHARGE_PROGRESS_TOLERANCE;

        if (chargeProgress < -CHARGE_PROGRESS_TOLERANCE) return false;

        float minimumProgress = Math.min(chargeData.getFireThreshold(), chargeData.getMaxCharge());
        if (chargeProgress + CHARGE_PROGRESS_TOLERANCE < minimumProgress) return false;
        if (chargeProgress > _getMaxReasonableChargeProgress(chargeData) + CHARGE_PROGRESS_TOLERANCE) return false;

        return true;
    }
    private float _getMaxReasonableChargeProgress(_ChargingData chargeData) {
        // 预留少量 tick 余量，用于容忍网络抖动和客户端/服务端调度偏差
        float elapsedTicks = Math.max(_getChargeElapsedMillis() / 50f, 0f) + CHARGE_TICK_TOLERANCE;
        float startProgress = _getChargeProgressAfterLastFire(chargeData);
        float maxProgress = startProgress + elapsedTicks * Math.max(chargeData.getChargePerTick(), 0f);
        return Math.min(maxProgress, chargeData.getMaxCharge());
    }
    private long _getChargeElapsedMillis() {
        if (this.shooterProperty.shootTimestamp >= 0) {
            long startTimestamp = this.shooterProperty.baseTimestamp + this.shooterProperty.shootTimestamp;
            return System.currentTimeMillis() - startTimestamp;
        }
        if (this.shooterProperty.drawTimestamp >= 0) {
            return System.currentTimeMillis() - this.shooterProperty.drawTimestamp;
        }
        return 0L;
    }
    private float _getChargeProgressAfterLastFire(_ChargingData chargeData) {
        if (this.shooterProperty.shootTimestamp < 0) return 0f;
        if (chargeData.getChargeType() == ChargeType.DELAY) return 0f; // delay 蓄力模式在客户端开火后总是重置
        return Math.max(0f, this.shooterProperty.chargeProgress - chargeData.getRecoverByFire());
    }

    /**
     * 以当前时间戳查询射击冷却。返回值一般不会超过枪械的射击间隔
     * @return 射击冷却
     */
    public long getShootCooldown() {
        return getShootCooldown(System.currentTimeMillis() - this.shooterProperty.baseTimestamp);
    }
    /**
     * 查询指定的 timestamp 下的射击冷却。根据情况返回值可能超过枪械的射击间隔。
     * @param timestamp 指定 timestamp，是偏移时间戳（基于base timestamp 的相对时间戳）
     * @return 射击冷却
     */
    public long getShootCooldown(long timestamp) {
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

    private void consumeAmmoFromPlayer(IGun iGun, ItemStack gunItem, boolean needCheckAmmo) {
        if (!needCheckAmmo) return;

        if (iGun.useDummyAmmo(gunItem)) {
            // TODO 这个逻辑是要统一在consumeAmmoOnce里处理的
            iGun.findAndExtractDummyAmmo(gunItem, 1);
        } else {
            IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(this.livingShooter, null);
            iGun.findAndExtractInventoryAmmo(inventoryCapability, gunItem, 1);
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

        GunPropertyCache gunPropertyCache = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$getGunPropertyCache();
        if (gunPropertyCache != null) {
            // TODO GunPropertyCache
        }
        _HeatData heatData = gunData.getHeatData();
        if (heatData != null) {
            rpm = (int) (rpm * iGun.lerpRPM(gunItem));
        }

        return 60_000L / rpm;
    }
}
