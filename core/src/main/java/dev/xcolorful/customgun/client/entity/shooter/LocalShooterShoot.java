/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.animation.statemachine.GunAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.api.animation.statemachine.GunAnimationState;
import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShootResult;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.event.gun.GunFireEvent;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.gun.*;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IMuzzleModifier;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.config.GunConfig;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterShoot;
import dev.xcolorful.customgun.core.gun.attack._DefaultGunFire;
import dev.xcolorful.customgun.core.network.message.ClientMessagePlayerShoot;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._ChargingData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public final class LocalShooterShoot extends LocalShooterAspect {

    private static final Predicate<ILivingShooter> SHOOT_LOCKED_CONDITION = operator -> operator.cgc$getSynShootCooldown() > 0;

    public LocalShooterShoot(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    /**
     * 充能，然后判断是否充能完毕
     */
    public boolean doCharge_isChargeEnough(boolean doShoot) {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.localShooterProperty.chargeProgress = 0f;
            return false;
        }

        // 检查是否有充能数据
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        @Nullable Map<FireModeType, _ChargingData> chargingDataMap = GunDataAccessor._getChargingData(iGun, gunItem);
        @Nullable _ChargingData chargeData = chargingDataMap != null ? chargingDataMap.get(fireModeType) : null;
        if (chargeData == null) return doShoot;

        boolean isChargeEnabled = chargeData.getEnableChargeDuringCooldown() || _getShootCooldown(iGun, gunItem) < SHOOT_COOLDOWN_MS;
        final float maxCharge = chargeData.getMaxCharge();
        boolean isChargeEnough = _isChargeEnough(doShoot, isChargeEnabled, chargeData, maxCharge);
        this.localShooterProperty.chargeProgress = Mth.clamp(this.localShooterProperty.chargeProgress, 0, maxCharge);
        return isChargeEnough;
    }
    private boolean _isChargeEnough(boolean doShoot, boolean isChargeEnabled,
                                    @NotNull _ChargingData chargeData, float maxCharge) {
        final float currentChargeProgress = this.localShooterProperty.chargeProgress;
        final ChargeType chargeType = chargeData.getChargeType();

        final boolean isCharging = (doShoot || (chargeType.unstoppableIfStarted() && currentChargeProgress > 0)) // 手动蓄力/自动蓄力
                && isChargeEnabled;
        final float alphaProgress = isCharging ? chargeData.getChargePerTick() : chargeData.getRecoverPerTick();

        final boolean isChargingBefore = this.localShooterProperty.isCharging; // 用于HOLD的回溯
        this.localShooterProperty.isCharging = isCharging;

        if (!isChargeEnabled) {
            // 减少蓄力进度并直接返回
            this.localShooterProperty.chargeProgress = currentChargeProgress - alphaProgress;
            return false;
        }

        assert isChargeEnabled = true;
        if (isCharging) {
            // 蓄力则增加进度
            this.localShooterProperty.chargeProgress = currentChargeProgress + alphaProgress;
            // 蓄满 且 蓄满自动开火 -> 充能足够
            return this.localShooterProperty.chargeProgress >= maxCharge && chargeType.autoShootIfCharged();
        } else if (currentChargeProgress > chargeData.getFireThreshold()) {
            // 不在蓄力，但充能足够
            this.localShooterProperty.isCharging = isChargingBefore;
            return true;
        } else {
            // 不在蓄力，且充能不够 -> 减少蓄力进度
            this.localShooterProperty.chargeProgress = currentChargeProgress - alphaProgress;
            return false;
        }
    }

    /**
     * 执行一次射击
     */
    public ShootResult shoot() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return ShootResult.NOT_GUN;

        @Nullable GunDisplayInstance gunDisplayInstance;
        GunData gunData;
        long cooldown; {
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunIndexInstance == null || gunDisplayInstance == null) return ShootResult.ID_NOT_EXIST;

            gunData = gunIndexInstance.getGunData();
            cooldown = _getShootCooldown(iGun, gunItem, gunData);
        }

        if ( // 2.1 检查状态锁
                // 如果上一次异步开火的效果还未执行，则直接返回
                !this.localShooterProperty.isShootRecorded
                // 射击冷却大于等于 1 tick 则不允许开火
                || cooldown >= 50
        ) return ShootResult.UNKNOWN_FAIL;

        // 如果状态锁正在准备锁定，且不是开火的状态锁，则不允许开火
        if (this.localShooterProperty.clientStateLock
                && this.localShooterProperty.lockedCondition != SHOOT_LOCKED_CONDITION
                && this.localShooterProperty.lockedCondition != null) {
            this.localShooterProperty.isShootRecorded = true;
            return ShootResult.IS_DRAWING; // 主要目的是防止切枪后开火动作覆盖切枪动作
        }

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if ( // 2.2 检查状态
                // 禁止射击的状态
                _shouldForceDisableShoot()
                // 近战冷却
                || iLivingShooter.cgc$getSynMeleeCooldown() > 0
                // 客户方防按键误触冷却 (已经在ShootKey利用ClientTickEvent触发了)
//                System.currentTimeMillis() - LocalShooterProperty.clientClickButtonTimestamp < SHOOT_COOLDOWN_MS
        ) {
            return ShootResult.UNKNOWN_FAIL;
        }

        boolean playDrySound = true;
        { // 3. IGunRuntime操作结果 -> Shooter状态
            /**
             * {@link IGunAttackRuntime#shooterFire}的默认实现为{@link IGunAttackRuntime#shooterFire}
             */
            @NotNull IGunAttackRuntime.ShooterFireResult shooterFireResult = iGun.shooterFire(null, iGun, gunItem, iLivingShooter, localShooter, null, null, this.localShooterProperty.chargeProgress);
            if (!shooterFireResult.isSuccess()) {
                return _onShooterFireFailed(shooterFireResult, gunDisplayInstance, playDrySound);
            }
            // 切换状态锁，不允许换弹、检视等行为进行
            this.localShooterProperty.lockState(SHOOT_LOCKED_CONDITION);
            this.localShooterProperty.isShootRecorded = false;
        }

        // 调用开火逻辑
        float finalChargeProgress = this.localShooterProperty.chargeProgress;
        this.doShoot(gunDisplayInstance, iGun, gunItem, gunData, cooldown, finalChargeProgress);

        this._recoverChargeAfterShoot(iGun, gunItem, gunData);
        return ShootResult.SUCCESS;
    }
    @ApiStatus.Internal
    private ShootResult _onShooterFireFailed(@NotNull IGunAttackRuntime.ShooterFireResult shooterFireResult,
                                             @NotNull GunDisplayInstance gunDisplayInstance,
                                             boolean playDrySound) {
        switch (shooterFireResult) {
            case OVERHEATED, NO_AMMO -> {
                if (playDrySound) {
                    SoundPlayManager.get().playShootSound(gunDisplayInstance.getGunSound(GunSoundType.DRY_FIRE_SOUND),
                            1.0f,
                            this.localShooter,
                            GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                            false);
                }
            }
            case NO_BARREL_AMMO -> {
                // 自动拉栓
                ILocalShooterGetter.fromLocalPlayer(this.localShooter).cgc$bolt();
            }
        }
        return ShootResult.UNKNOWN_FAIL;
    }

    /**
     * 对应{@link LivingShooterShoot#_shouldForceDisableShoot}
     */
    private boolean _shouldForceDisableShoot() {
        if (LivingShooterShoot.isIllegalShootState(this.localShooter)) return true;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if ( // 2.2 检查状态
                // 正在换弹
                iLivingShooter.cgc$getSynReloadState().getStateType().isReloading()
                // 正在切枪
                || iLivingShooter.cgc$getSynDrawCooldown() > 0
                // 正在拉栓
                || iLivingShooter.cgc$getSynIsBolting()
                // 正在疾跑
                || iLivingShooter.cgc$getSynSprintTime() > 0
        ) return true;

        return false;
    }

    private void _recoverChargeAfterShoot(IGun iGun, ItemStack gunItem,
                                          GunData gunData) {
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        @Nullable _ChargingData chargeData = gunData.getChargingData().get(fireModeType);
        if (chargeData == null) return;

        ChargeType chargeType = chargeData.getChargeType();
        if (chargeType.resetChargeAfterShoot()) {
            this.localShooterProperty.chargeProgress = 0f;
        } else { // 其他类型则正常恢复蓄力
            this.localShooterProperty.chargeProgress = Math.max(0f, this.localShooterProperty.chargeProgress - chargeData.getRecoverByFire());
        }
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
        long period = fireModeType == FireModeType.BURST ? _DefaultGunFire._getBurstShootIntervalMs(gunData) : 1;
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
                if (CustomGun.getEventPoster().postCustomEvent(new GunFireEvent(McLogicalSide.CLIENT,
                        iGun, gunItem, iLivingShooter, localPlayer))) {
                    return;
                }

//                /**
//                 * {@link IGunAttackRuntime#gunFire}的默认实现为{@link IGunAttackRuntime#gunFire}
//                 */
//                @NotNull IGunAttackRuntime.GunFireResult gunFireResult = iGun.gunFire(null, iGun, gunItem, iLivingShooter, localPlayer, null, null);
//                if (!gunFireResult.isSuccess()) {
//                    return;
//                }

                // 动画和声音循环播放
                LuaAnimStateMachine<GunAnimStateContext> animStateMachine = gunDisplayInstance.getAnimStateMachine();
                animStateMachine.trigger(GunAnimationState.INPUT_SHOOT.getConstantName());

                // 开火需要打断检视
                SoundPlayManager.get().stopMainTrackSound(gunDisplayInstance, GunSoundType.INSPECT_SOUND);

                if (_useSuppressedSound()) {
                    SoundPlayManager.get().playShootSound(
                            gunDisplayInstance.getGunSound(GunSoundType.SILENCE_SOUND),
                            0.6f,
                            this.localShooter,
                            GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * gunData.getFireSoundData().getSilencedMultiplier(),
                            false);
                } else {
                    SoundPlayManager.get().playShootSound(
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
    private boolean _useSuppressedSound() {
        ShooterGunModifierCache shooterGunModifierCache = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter).cgc$getGunModifierCache();
        if (shooterGunModifierCache == null) return false;

        @Nullable FireSoundType fireSoundType = IMuzzleModifier.getValue(shooterGunModifierCache, AttachmentModifierType.MUZZLE);
        return fireSoundType != null && fireSoundType.isSoundSuppressed();
    }

    public long getClientShootCooldown() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return -1;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return -1;

        return _getShootCooldown(iGun, gunItem, gunIndexInstance.getGunData());
    }
    private long _getShootCooldown(IGun iGun, ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return -1;

        GunData gunData = gunIndexInstance.getGunData();
        return _getShootCooldown(iGun, gunItem, gunData);
    }
    private long _getShootCooldown(IGun iGun, ItemStack gunItem, @NotNull GunData gunData) {
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        long shootInterval = fireModeType == FireModeType.BURST
                ? (long) (gunData.getBurstData().getShootIntervalSeconds() * 1000f)
                : LivingShooterShoot._getShootInterval(this.localShooter, gunData, fireModeType, iGun, gunItem);

        long cooldown = shootInterval - (System.currentTimeMillis() - this.localShooterProperty.clientShootTimestamp);
        return Math.max(cooldown, 0);
    }
}
