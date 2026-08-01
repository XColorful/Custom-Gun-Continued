/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.entity.shooter.ILocalShooterGetter;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.gun.attack.IGunAttackRuntime;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.ChargeType;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.config.GunConfig;
import xiao.customgun.core.entity.shooter.LivingShooterShoot;
import xiao.customgun.core.gun.attack.GunAttackManager;
import xiao.customgun.core.network.message.ClientMessagePlayerShoot;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._BurstData;
import xiao.customgun.core.resource.data.data.gun._ChargingData;
import xiao.customgun.core.util.SendUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public final class LocalShooterShoot extends LocalShooterAspect {

    private static final Predicate<ILivingShooter> SHOOT_LOCKED_CONDITION = operator -> operator.cgc$getSynShootCooldown() > 0;

    public LocalShooterShoot(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public boolean chargeAndGetResult(boolean doShoot) {
        // 因为开火冷却检测用了特别定制的方法，所以不检查状态锁，而是手动检查是否换弹、切枪
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.localShooterProperty.chargeProgress = 0f;
            return false;
        }

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (clientGunIndexInstance == null || gunDisplayInstance == null) return false;

        @Nullable GunData gunData = clientGunIndexInstance.getGunData();
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        @Nullable _ChargingData chargeData = gunData != null ? gunData.getChargingData().get(fireModeType) : null;
        if (chargeData == null) return doShoot;

        boolean canChargeDuringCooldown = chargeData.getEnableChargeDuringCooldown()
                || _getShootCooldown(iGun, gunItem, gunData) < SHOOT_COOLDOWN_MS;
        boolean canCharge = canChargeDuringCooldown
                && preCheckError(iGun, gunItem, gunDisplayInstance, gunData, doShoot) == null;
        float chargeProgress = this.localShooterProperty.chargeProgress;
        ChargeType type = chargeData.getChargeType();

        switch (type) {
            case AUTO -> {
                if (doShoot && canCharge) {
                    this.localShooterProperty.isCharging = true;
                    this.localShooterProperty.chargeProgress = Math.min(chargeProgress + chargeData.getChargePerTick(), chargeData.getMaxCharge());
                    return this.localShooterProperty.chargeProgress >= chargeData.getMaxCharge();
                } else {
                    this.localShooterProperty.isCharging = false;
                    this.localShooterProperty.chargeProgress = Math.max(chargeProgress - chargeData.getRecoverPerTick(), 0f);
                }
            }
            case HOLD -> {
                if (doShoot && canCharge) {
                    this.localShooterProperty.isCharging = true;
                    this.localShooterProperty.chargeProgress = Math.min(chargeProgress + chargeData.getChargePerTick(), chargeData.getMaxCharge());
                } else {
                    if (canChargeDuringCooldown && chargeProgress >= chargeData.getFireThreshold()) {
                        return true;
                    }
                    this.localShooterProperty.isCharging = false;
                    this.localShooterProperty.chargeProgress = Math.max(chargeProgress - chargeData.getRecoverPerTick(), 0f);
                }
            }
            case DELAY -> {
                if ((doShoot || chargeProgress > 0) && canCharge) {
                    this.localShooterProperty.isCharging = true;
                    this.localShooterProperty.chargeProgress = Math.min(chargeProgress + chargeData.getChargePerTick(), chargeData.getMaxCharge());
                    return this.localShooterProperty.chargeProgress >= chargeData.getMaxCharge();
                } else {
                    this.localShooterProperty.isCharging = false;
                    this.localShooterProperty.chargeProgress = Math.max(chargeProgress - chargeData.getRecoverPerTick(), 0f);
                }
            }
            // 添加类型使此处强制编译不通过
        }
        return false;
    }

    public ShootResult shoot() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return ShootResult.NOT_GUN;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (clientGunIndexInstance == null || gunDisplayInstance == null) return ShootResult.ID_NOT_EXIST;

        GunData gunData = clientGunIndexInstance.getGunData();
        if (gunData == null) return ShootResult.UNKNOWN_FAIL;

        long coolDown = _getShootCooldown(iGun, gunItem, gunData);

        // 如果上一次异步开火的效果还未执行，则直接返回
        if (!this.localShooterProperty.isShootRecorded) return ShootResult.COOL_DOWN;
        // 如果状态锁正在准备锁定，且不是开火的状态锁，则不允许开火
        if (this.localShooterProperty.clientStateLock
                && this.localShooterProperty.lockedCondition != SHOOT_LOCKED_CONDITION
                && this.localShooterProperty.lockedCondition != null) {
            this.localShooterProperty.isShootRecorded = true;
            return ShootResult.IS_DRAWING; // 主要目的是防止切枪后开火动作覆盖切枪动作
        }

        // 如果射击冷却大于等于 1 tick 则不允许开火
        if (coolDown >= 50) return ShootResult.COOL_DOWN;

        // 基础检查
        ShootResult errorResult = preCheckError(iGun, gunItem, gunDisplayInstance, gunData, true);
        if (errorResult != null) return errorResult;

        // 检查是否正在奔跑
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if (iLivingShooter.cgc$getSynSprintTime() > 0) return ShootResult.IS_SPRINTING;


        { // 3. IGunRuntime操作结果 -> Shooter状态
            /**
             * {@link IGunAttackRuntime#shooterFire}的默认实现为{@link GunAttackManager#shooterFire}
             */
            @NotNull IGunAttackRuntime.ShooterFireResult shooterFireResult = iGun.shooterFire(null, iGun, gunItem, iLivingShooter, localShooter, null, null);
            if (!shooterFireResult.isSuccess()) {
                return ShootResult.UNKNOWN_FAIL;
            }
        }

        // 切换状态锁，不允许换弹、检视等行为进行
        this.localShooterProperty.lockState(SHOOT_LOCKED_CONDITION);
        this.localShooterProperty.isShootRecorded = false;
        // 调用开火逻辑
        float finalChargeProgress = this.localShooterProperty.chargeProgress;
        this.doShoot(gunDisplayInstance, iGun, gunItem, gunData, coolDown, finalChargeProgress);

        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        @Nullable _ChargingData chargeData = gunData.getChargingData().get(fireModeType);
        if (chargeData != null) {
            if (chargeData.getChargeType() == ChargeType.DELAY) {
                this.localShooterProperty.chargeProgress = 0f;
            } else {
                this.localShooterProperty.chargeProgress = Math.max(0f, this.localShooterProperty.chargeProgress - chargeData.getRecoverByFire());
            }
        }

        return ShootResult.SUCCESS;
    }

    @Nullable
    private ShootResult preCheckError(IGun iGun, ItemStack gunItem,
                                      GunDisplayInstance gunDisplayInstance, GunData gunData, boolean playDrySound) {
        // 按钮冷却时间未到，防止误触
        if (System.currentTimeMillis() - LocalShooterProperty.clientClickButtonTimestamp < SHOOT_COOLDOWN_MS) {
            return ShootResult.COOL_DOWN;
        }

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        // 检查是否正在换弹
        if (iLivingShooter.cgc$getSynReloadState().getStateType().isReloading()) return ShootResult.IS_RELOADING;
        // 检查是否正在切枪
        if (iLivingShooter.cgc$getSynDrawCooldown() > 0) return ShootResult.IS_DRAWING;
        // 检查是否正在拉栓
        if (iLivingShooter.cgc$getSynIsBolting()) return ShootResult.IS_BOLTING;

        // 判断是否处于近战冷却时间
        if (iLivingShooter.cgc$getSynMeleeCooldown() > 0) return ShootResult.IS_MELEE;

        // 检查过热锁
        if (iGun.hasHeat(gunItem)) {
            if (iGun.hasOverheatLock(gunItem)) {
                if (playDrySound) {
                    SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.DRY_FIRE_SOUND),
                            1.0f,
                            this.localShooter,
                            GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                            false);
                }
                return ShootResult.OVERHEATED;
            }
        }

        // 检查消耗子弹
        BoltType boltType = gunData.getBoltType();
        boolean useInventoryAmmo = iGun.useInventoryAmmo(gunItem); // 是否为背包直读
        boolean hasAmmo = useInventoryAmmo ? iGun.hasInventoryAmmo(this.localShooter, gunItem)
                : iGun.getMagAmmoCountWithBarrel(gunItem, boltType) > 0;
        if (!hasAmmo) {
            if (playDrySound) {
                SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.DRY_FIRE_SOUND),
                        1.0f,
                        this.localShooter,
                        GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                        false);
            }
            return ShootResult.NO_AMMO;
        }
        switch (boltType) {
            case MANUAL_ACTION -> {
                if (!iGun.hasBarrelAmmo(gunItem)) {
                    ILocalShooterGetter.fromLocalPlayer(this.localShooter).cgc$bolt();
                    return ShootResult.NEED_BOLT;
                }
            }
        }

        return null;
    }

    private void doShoot(GunDisplayInstance gunDisplayInstance, IGun iGun, ItemStack gunItem,
                         GunData gunData, long delay, float chargeProgress) {
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        BoltType boltType = gunData.getBoltType();
        // 获取总余弹数
        boolean consumeAmmo = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter).cgc$consumesAmmoOrNot();
        int ammoCount = consumeAmmo ? Integer.MAX_VALUE
                : iGun.getMagAmmoCountWithBarrel(gunItem, boltType);
        // 连发射击间隔
        long period = fireModeType == FireModeType.BURST ? _getBurstShootInterval(gunData) : 1;
        // 最大连发数
        final int maxCount = Math.min(ammoCount, fireModeType == FireModeType.BURST ? gunData.getBurstData().getBurstAmount() : 1);
        // 连发计数器
        AtomicInteger count = new AtomicInteger(0);

        LocalShooterProperty.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (count.get() == 0) {
                // 转换 isRecord 状态，允许下一个tick的开火检测
                this.localShooterProperty.isShootRecorded = true;
            }

            // 处理过热数据
            if (gunData.getHeatData() != null) {
                if (iGun.hasOverheatLock(gunItem)) {
                    ScheduledFuture<?> future = (ScheduledFuture<?>) Thread.currentThread();
                    future.cancel(false); // 取消当前任务
                    return;
                }
            }
            // 如果达到最大连发次数，或者玩家已经死亡，取消任务
            if (count.get() >= maxCount || this.localShooter.isDeadOrDying()) {
                ScheduledFuture<?> future = (ScheduledFuture<?>) Thread.currentThread();
                future.cancel(false); // 取消当前任务
                return;
            }

            // 以下逻辑只需要执行一次
            if (count.get() == 0) {
                // 如果状态锁正在准备锁定，且不是开火的状态锁，则不允许开火(主要用于防止切枪后开火动作覆盖切枪动作)
                if (this.localShooterProperty.clientStateLock
                        && this.localShooterProperty.lockedCondition != SHOOT_LOCKED_CONDITION
                        && this.localShooterProperty.lockedCondition != null) {
                    return;
                }
                // 记录新的开火时间戳
                this.localShooterProperty.clientLastShootTimestamp = this.localShooterProperty.clientShootTimestamp;
                this.localShooterProperty.clientShootTimestamp = System.currentTimeMillis();
                SendUtils.sendMessageToServer(new ClientMessagePlayerShoot(
                        this.localShooterProperty.clientShootTimestamp - this.localShooterProperty.clientBaseTimestamp,
                        chargeProgress)
                );
            }

//            // todo需要检查
            // TODO ↑tmd原模组这么乱一坨屎山检查个毛线 :(
            Minecraft.getInstance().submitAsync(() -> {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer == null) return;

                ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(localPlayer);

                /**
                 * {@link IGunAttackRuntime#gunFire}的默认实现为{@link IGunAttackRuntime#gunFire}
                 */
                @NotNull IGunAttackRuntime.GunFireResult gunFireResult = iGun.gunFire(null, iGun, gunItem, iLivingShooter, localPlayer, null, null);
                if (!gunFireResult.isSuccess()) {
                    return;
                }

                // 动画和声音循环播放
                // TODO GunDisplayInstance AnimationStateMachine
                if (false) {
                }
                // 开火需要打断检视
                SoundPlayManager.get().stopCurrentSound(gunDisplayInstance, GunSoundType.INSPECT_SOUND);

                if (_useSilenceSound()) {
                    SoundPlayManager.get().playGunSound(
                            gunDisplayInstance.getGunSound(GunSoundType.SILENCE_SOUND),
                            0.6f,
                            this.localShooter,
                            GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * gunData.getFireSoundData().getSilencedMultiplier(),
                            false);
                } else {
                    SoundPlayManager.get().playGunSound(
                            gunDisplayInstance.getGunSound(GunSoundType.SHOOT_SOUND),
                            0.8f,
                            this.localShooter,
                            GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * gunData.getFireSoundData().getNormalMultiplier(),
                            false);
                }
            });

            count.getAndIncrement();
        }, delay, period, TimeUnit.MILLISECONDS);
    }
    private long _getBurstShootInterval(GunData gunData) {
        _BurstData burstData = gunData.getBurstData();
        int bpm = burstData.getBpm();
        return bpm > 0 ? 60_000L / bpm
                : 300; // 为避免非法运算，随意返回一个默认值
    }
    private boolean _useSilenceSound() {
        ShooterGunModifierCache shooterGunModifierCache = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter).cgc$getGunModifierCache();
        if (shooterGunModifierCache == null) return false;

        // TODO GunPropertyCache SilenceModifier.ID
        return false;
    }

    public long getClientShootCooldown() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return -1;

        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(iGun.getGunLocation(gunItem));
        if (clientGunIndexInstance == null) return -1;

        return _getShootCooldown(iGun, gunItem, clientGunIndexInstance.getGunData());
    }
    private long _getShootCooldown(IGun iGun, ItemStack gunItem, GunData gunData) {
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        long coolDown = fireModeType == FireModeType.BURST
                ? (long) (gunData.getBurstData().getShootIntervalSeconds() * 1000f) - (System.currentTimeMillis() - this.localShooterProperty.clientShootTimestamp)
                : LivingShooterShoot._getShootInterval(this.localShooter, gunData, fireModeType, iGun, gunItem);
        return Math.max(coolDown, 0);
    }
}
