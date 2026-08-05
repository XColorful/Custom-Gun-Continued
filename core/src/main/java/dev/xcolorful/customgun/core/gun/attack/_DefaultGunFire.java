/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.attack;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShootState;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.projectile.IGunProjectileGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.event.gun.GunFireEvent;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.item.gun.FireSoundType;
import dev.xcolorful.customgun.core.api.item.gun.modifier.*;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.sound.gun.GunSoundTypeTag;
import dev.xcolorful.customgun.core.config.AmmoConfig;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import dev.xcolorful.customgun.core.init.registry.ModEntities;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageGunFire;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MuzzleModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._BulletData;
import dev.xcolorful.customgun.core.resource.data.data.gun._BurstData;
import dev.xcolorful.customgun.core.resource.data.data.gun._FireSoundData;
import dev.xcolorful.customgun.core.resource.data.data.gun._HeatData;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.sound.SoundManager;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class _DefaultGunFire {

    /*
    目前是基本照搬原模组代码，待重构
     */
    public static @Nullable IGunAttackRuntime.GunFirePropertyCache _getGunFireContext(@Nullable ShooterProperty shooterProperty,
                                                                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                                                      ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                                                      @NotNull GunData gunData) {
        IGunAttackRuntime.GunFirePropertyCache context = new IGunAttackRuntime.GunFirePropertyCache();

        @Nullable ShooterGunModifierCache shooterGunModifierCache = iLivingShooter.cgc$getGunModifierCache();

        { // 枪械数据
            var gunLocation = iGun.getGunLocation(gunItem);
            var gunDisplayLocation = iGun.getGunDisplayLocation(gunItem);
            var ammoLocation = gunData.getAmmoLocation();

            context.gunLocation = gunLocation;
            context.gunDisplayLocation = gunDisplayLocation;
            context.ammoLocation = ammoLocation;
        }

        { // 不准确度
            @Nullable _HeatData heatData = gunData.getHeatData();
            float heatInaccuracy = 1f;
            if (heatData != null) {
                float maxHeat = heatData.getMaxHeat();;
                float heatCount = iGun.getHeatCount(gunItem);
                heatInaccuracy *= Mth.lerp(heatCount / maxHeat, heatData.getMinInaccuracyByHeat(), heatData.getMaxInaccuracyByHeat());
            }

            ShootState shootState = ShootState.fromLivingShooter(iLivingShooter, livingShooter);
            float inaccuracy = 1f;
            if (shooterGunModifierCache != null) {
                @Nullable Float _inaccuracy = switch (shootState) {
                    case AIM -> IAimInaccuracyModifier.getValue(shooterGunModifierCache, AttachmentModifierType.AIM_INACCURACY);
                    case SNEAK -> ISneakInaccuracyModifier.getValue(shooterGunModifierCache, AttachmentModifierType.SNEAK_INACCURACY);
                    case PRONE -> IProneInaccuracyModifier.getValue(shooterGunModifierCache, AttachmentModifierType.PRONE_INACCURACY);
                    default -> IOtherInaccuracyModifier.getValue(shooterGunModifierCache, AttachmentModifierType.OTHER_INACCURACY);
                };
                if (_inaccuracy != null) inaccuracy = _inaccuracy;
            }
            context.inaccuracy = inaccuracy * heatInaccuracy;
        }

        { // 声音距离
            _FireSoundData fireSoundData = gunData.getFireSoundData();
            context.soundDistance = fireSoundData.getNormalMultiplier();
            context.silenceSound = false;

            var attachmentLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.MUZZLE);
            @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(attachmentLocation);
            if (attachmentIndexInstance != null) {
                AttachmentData attachmentData = attachmentIndexInstance.getAttachmentData();
                @Nullable _MuzzleModifierData muzzleModifier = attachmentData.getMuzzleModifier();
                if (muzzleModifier != null) {
                    FireSoundType fireSoundType = muzzleModifier.getFireSoundType();
                    switch (fireSoundType) {
                        case SILENCED -> {
                            context.soundDistance = fireSoundData.getSilencedMultiplier();
                            context.silenceSound = true;
                        }
                        case MUTED -> context.soundDistance = 0;
                    }
                }
            }
        }

        { // 子弹飞行速度
            if (shooterGunModifierCache != null) {
                @Nullable Float _speed = IBulletSpeedModifier.getValue(shooterGunModifierCache, AttachmentModifierType.BULLET_SPEED);
                float speed = _speed != null ? _speed : 1;
                speed *= AmmoConfig.GLOBAL_BULLET_SPEED_MODIFIER.get();
                context.bulletSpeed = Mth.clamp((speed / 20f), 0f, Float.MAX_VALUE);
            }
        }

        { // 弹丸数量
            _BulletData bulletData = gunData.getBulletData();
            context.bulletSplitAmount = bulletData.getBulletSplitAmount();
        }

        { // 连发数量 + 连发间隔
            FireModeType fireModeType = iGun.getFireModeType(gunItem);
            if (fireModeType == FireModeType.BURST) {
                _BurstData burstData = gunData.getBurstData();
                context.shootCount = burstData.getBurstAmount();
                context.shootIntervalMs = _getBurstShootIntervalMs(gunData);
            } else {
                context.shootCount = 1;
                context.shootIntervalMs = 50;
            }
        }

        return context;
    }
    @ApiStatus.Internal
    public static long _getBurstShootIntervalMs(GunData gunData) {
        _BurstData burstData = gunData.getBurstData();
        int bpm = burstData.getBpm();
        return bpm > 0 ? 60_000L / bpm
                : 300; // 为避免非法运算，随意返回一个默认值
    }

    @ApiStatus.Internal
    public static IGunAttackRuntime.GunFireResult doGunFire(IGunAttackRuntime.GunFirePropertyCache context,
                                                            IGun iGun, ItemStack gunItem,
                                                            ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                            Supplier<Float> pitch, Supplier<Float> yaw,
                                                            GunData gunData) {
        { // 0. 枪械数据异常
            if (
                // 射击者死亡
                    livingShooter == null
                            // 射击者死亡
                            || livingShooter.isDeadOrDying()
                            // 主手不再持该枪械
                            || !livingShooter.getMainHandItem().equals(gunItem) || livingShooter.getMainHandItem().isEmpty()
            ) return IGunAttackRuntime.GunFireResult.ERROR;
        }

        { // 事件跳过
            if (CustomGun.getEventPoster().postCustomEvent(new GunFireEvent(McLogicalSide.SERVER,
                    iGun, gunItem, iLivingShooter, livingShooter))) {
                return IGunAttackRuntime.GunFireResult.SUCCESS;
            } else {
                SendUtils.sendMessageToTrackingEntity(livingShooter, new ServerMessageGunFire(livingShooter.getId(), gunItem));
            }
        }

        { // 1. 过热锁 + 过热处理
            // TODO
        }

        BoltType boltType = gunData.getBoltType();
        { // 2. 消耗子弹
            int consumedAmmo = iGun.consumeAmmoOnce(livingShooter, gunItem, boltType);
            if (consumedAmmo <= 0) {
                return IGunAttackRuntime.GunFireResult.AMMO_CONSUME_FAILED;
            }
        }

        { // 生成子弹
            // 获取射击方向（pitch 和 yaw）
            float currentPitch = pitch != null ? pitch.get() : livingShooter.getXRot();
            float currentYaw = yaw != null ? yaw.get() : livingShooter.getYRot();

            Level level = livingShooter.level();

            for (int i = 0; i < context.bulletSplitAmount; i++) {
                GunProjectile gunProjectile = new GunProjectile(ModEntities.GUN_PROJECTILE.get(), level,
                        livingShooter,
                        context.gunLocation, context.gunDisplayLocation, context.ammoLocation);
                // TODO GunProjectile applyShotgunDamageSpread, setShotDamageMultiplier
                iGun.doBulletSpread(iLivingShooter.cgc$getShooterProperty(),
                        iGun, gunItem,
                        iLivingShooter, livingShooter,
                        IGunProjectileGetter.fromEntity(gunProjectile), gunProjectile,
                        i,
                        currentPitch, currentYaw, context.bulletSpeed, context.inaccuracy);
                level.addFreshEntity(gunProjectile);
            }
        }

        { // 播放枪声
            if (context.soundDistance > 0) {
                String soundTypeTag = context.silenceSound ? GunSoundTypeTag.SILENCE_3P_SOUND : GunSoundTypeTag.SHOOT_3P_SOUND;
                SoundManager.sendSoundToNearby(livingShooter, (int) context.soundDistance,
                        context.gunLocation, context.gunDisplayLocation,
                        soundTypeTag, 0.8f, 0.9f + livingShooter.getRandom().nextFloat() * 0.125f);

            }
        }

        return IGunAttackRuntime.GunFireResult.SUCCESS;
    }
}
