/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.attack;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.event.CycledEvent;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.item.gun.GunDataAccessor;
import dev.xcolorful.customgun.core.api.item.gun.MeleeType;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.projectile.physics.ProjectilePhysicsManager;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MeleeModifierData;
import dev.xcolorful.customgun.core.resource.data.data.attachment.melee._TargetEffectData;
import dev.xcolorful.customgun.core.resource.data.data.gun._ChargingData;
import dev.xcolorful.customgun.core.resource.data.data.gun._MeleeData;
import dev.xcolorful.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@ApiStatus.Internal
public class _DefaultGunAttack {

    // public仅用于文档链接
    @ApiStatus.Internal
    public    static IGunAttackRuntime.ShooterFireResult shooterFire(McLogicalSide logicalSide,
                                                                     ShooterProperty shooterProperty,
                                                                     @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                                     ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                                     Supplier<Float> pitch, Supplier<Float> yaw,
                                                                     float clientChargeProgress) {
        // 0. 枪械数据异常
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return IGunAttackRuntime.ShooterFireResult.ERROR;

        GunData gunData = gunIndexInstance.getGunData();

        { // 1. 检查过热锁
            if (iGun.hasHeat(gunItem) && iGun.hasOverheatLock(gunItem)) {
                return IGunAttackRuntime.ShooterFireResult.OVERHEATED;
            }
        }

        BoltType boltType = gunData.getBoltType();
        { // 2. 检查消耗子弹
            boolean useInventoryAmmo = iGun.useInventoryAmmo(gunItem); // 是否为背包直读
            boolean hasAmmo = useInventoryAmmo ? iGun.hasInventoryAmmo(livingShooter, gunItem)
                    : iGun.getMagAmmoCountWithBarrel(gunItem, boltType) > 0;
            if (!hasAmmo) return IGunAttackRuntime.ShooterFireResult.NO_AMMO;
        }

        { // 3. 检查拉栓
            if (boltType.useBarrelAmmo()) {
                if (!boltType.autoBoltBarrelAmmo()) {
                    // 检查枪管是否有子弹 (否则要拉栓)
                    if (!iGun.hasBarrelAmmo(gunItem)) {
                        return IGunAttackRuntime.ShooterFireResult.NO_BARREL_AMMO;
                    }
                } else {
                    // 服务端在gunFire处理自动上弹，客户端不更新本地数据
                }
            }
        }

        // 客户端侧提前返回，以继续客户端逻辑
        if (logicalSide.isClient()) return IGunAttackRuntime.ShooterFireResult.SUCCESS;

        // --------服务端--------

        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        @Nullable Map<FireModeType, _ChargingData> chargingDataMap = gunData.getChargingData();
        _ChargingData chargingData = chargingDataMap != null ? chargingDataMap.get(fireModeType) : null;
        { // 4. 蓄力进度检查
            if (!_DefaultGunCharge.isChargeProgressAcceptable(shooterProperty, chargingData, clientChargeProgress)) {
                return IGunAttackRuntime.ShooterFireResult.NOT_CHARGED;
            }
        }

        // --------收尾--------
        final @NotNull IGunAttackRuntime.ShooterFireResult result = IGunAttackRuntime.ShooterFireResult.SUCCESS; {
            // 蓄力进度clamp更正
            shooterProperty.chargeProgress = _DefaultGunCharge.clampChargeProgress(shooterProperty, chargingData, clientChargeProgress);
        } return result;
    }

    /**
     * 对应原模组{@code AbstractGunItem.shoot()}，仅服务端触发
     */
    protected static IGunAttackRuntime.GunFireResult gunFire(@Nullable ShooterProperty shooterProperty,
                                                             @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                             ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                             Supplier<Float> pitch, Supplier<Float> yaw) { // TODO 这两个参数写到GunScriptApi还是lua函数参数?
        // 0. 枪械数据异常
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return IGunAttackRuntime.GunFireResult.ERROR;

        GunData gunData = gunIndexInstance.getGunData();

        @Nullable IGunAttackRuntime.GunFirePropertyCache gunFirePropertyCache = _DefaultGunFire._getGunFireContext(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, gunData);
        if (gunFirePropertyCache == null) return IGunAttackRuntime.GunFireResult.ERROR;

        BooleanSupplier shootTask = () -> _DefaultGunFire.doGunFire(gunFirePropertyCache,
                iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw, gunData)
                .isSuccess();
        CycledEvent.create(shootTask, 0, gunFirePropertyCache.shootIntervalMs, gunFirePropertyCache.shootCount);

        return IGunAttackRuntime.GunFireResult.SUCCESS;
    }

    protected static void doBulletSpread(LivingEntity livingShooter,
                                         @NotNull IGunProjectile iGunProjectile, @NotNull Projectile projectile,
                                         float xRot, float yRot, float pow, float uncertainty) {
        float spread = ProjectilePhysicsManager.VANILLA_SPREAD_SCALE * uncertainty;
        RandomSource random = livingShooter.getRandom();

        Vec2 spreadOffset = new Vec2(
                (float) random.triangle(0.0F, spread),
                (float) random.triangle(0.0F, spread)
        );

        iGunProjectile.shootFromRotation(livingShooter, projectile, xRot, yRot, 0, pow, spreadOffset);
    }

    protected static @Nullable IGunAttackRuntime.MeleePreparation prepareMelee(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                                               ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        @Nullable MeleeType meleeType = iGun.getGunMeleeType(gunItem);
        if (meleeType == null) return null;

        return switch (meleeType) {
            case BAYONET, STOCK -> _prepareAttachmentMelee(iGun, gunItem, meleeType);
            case PUSH -> {
                _DefaultMeleeData defaultMeleeData = GunDataAccessor._getGunDefaultMeleeData(iGun, gunItem);
                if (defaultMeleeData == null) yield null;

                float damageDelaySeconds = defaultMeleeData.getDamageDelaySeconds();
                int prepareTick = (int) Math.max(0, damageDelaySeconds * 20);
                yield new IGunAttackRuntime.MeleePreparation(prepareTick, MeleeType.PUSH);
            }
            // 增加类型使此处强制编译不通过
        };
    }
    private static @Nullable IGunAttackRuntime.MeleePreparation _prepareAttachmentMelee(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                                                        @NotNull MeleeType meleeType) {
        AttachmentCategory attachmentCategory = switch (meleeType) {
            case BAYONET -> AttachmentCategory.MUZZLE;
            case STOCK -> AttachmentCategory.STOCK;
            default -> throw new IllegalArgumentException("_DefaultGunAttack: can't handle melee type " + meleeType);
        };

        @Nullable _MeleeModifierData meleeModifierData = GunDataAccessor._getAttachmentMeleeModifierData(iGun, gunItem, attachmentCategory);
        if (meleeModifierData == null) return null;

        float damageDelaySeconds = meleeModifierData.getDamageDelaySeconds();
        int prepareTick = (int) Math.max(0, damageDelaySeconds * 20);

        return new IGunAttackRuntime.MeleePreparation(prepareTick, meleeType);
    }

    protected static int melee(ShooterProperty shooterProperty,
                                @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                MeleeType meleeType) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        _MeleeData meleeData = gunData.getMeleeData();
        float gunBaseLength = meleeData.getGunBaseLength();

        return switch (meleeType) {
            case BAYONET -> _doAttachmentMelee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter,
                    gunBaseLength, AttachmentCategory.MUZZLE);
            case STOCK -> _doAttachmentMelee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter,
                    gunBaseLength, AttachmentCategory.STOCK);
            case PUSH -> {
                _DefaultMeleeData defaultMeleeData = meleeData.getDefaultMeleeData();
                yield _doGunMelee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter,
                    gunBaseLength, defaultMeleeData);
            }
            // 增加类型使此处强制编译不通过
        };
    }
    private static int _doAttachmentMelee(ShooterProperty shooterProperty,
                                              @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                              ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                              float gunBaseLength, AttachmentCategory attachmentCategory) {
        @Nullable _MeleeModifierData meleeModifierData = GunDataAccessor._getAttachmentMeleeModifierData(iGun, gunItem, attachmentCategory);
        if (meleeModifierData == null) return 0;

        _doMelee(iLivingShooter, livingShooter,
                gunBaseLength,
                meleeModifierData.getMeleeDistance(), meleeModifierData.getRangeAngle(), meleeModifierData.getKnockbackStrength(), meleeModifierData.getMeleeDamage(), meleeModifierData.getTargetEffect());
        return 1;
    }
    private static int _doGunMelee(ShooterProperty shooterProperty,
                                       @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                       ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                       float gunBaseLength, _DefaultMeleeData defaultMeleeData) {
        _doMelee(iLivingShooter, livingShooter,
                gunBaseLength,
                defaultMeleeData.getMeleeDistance(), defaultMeleeData.getRangeAngle(), defaultMeleeData.getKnockbackStrength(), defaultMeleeData.getMeleeDamage(), null);
        return 1;
    }
    private static void _doMelee(ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                 float gunBaseLength,
                                 float meleeDistance, float rangeAngle, float knockback, float meleeDamage, @Nullable List<_TargetEffectData> targetEffects) {
        if (PlannedRefactor.SPECIAL_MELEE_RANGE_CALCULATION) return;

        // 先扣饱和度
        if (livingShooter instanceof ServerPlayer serverPlayer) {
            serverPlayer.causeFoodExhaustion(0.1F);
        }

        // 枪长 + 刺刀扩展
        float damageDistance = gunBaseLength + meleeDistance;
        float xRot = (float) Math.toRadians(-livingShooter.getXRot());
        float yRot = (float) Math.toRadians(-livingShooter.getYRot());
        // 视角向量
        Vec3 eyeVec = new Vec3(0, 0, 1).xRot(xRot).yRot(yRot).normalize().scale(damageDistance);
        // 球心坐标
        Vec3 centerPos = livingShooter.getEyePosition().subtract(eyeVec);
        // 先获取范围内所有的实体
        if (!(livingShooter.level() instanceof ServerLevel serverLevel)) return;
        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, livingShooter.getBoundingBox().inflate(damageDistance));
        Supplier<Float> realDamage = () -> meleeDamage;

        // 而后检查是否在锥形范围内
        for (LivingEntity victimEntity : nearbyEntities) {
            // 先计算出球心->目标向量
            Vec3 targetVec = victimEntity.getEyePosition().subtract(centerPos);
            // 目标到球心距离
            double targetLength = targetVec.length();
            // 距离在一倍距离之内的，在玩家背后，不进行伤害
            if (targetLength < meleeDistance) continue;

            // 计算出向量夹角
            double degree = Math.toDegrees(
                    Math.acos(targetVec.dot(eyeVec) / (targetLength * damageDistance) )
            );
            // 向量夹角在范围内的，才能进行伤害
            if (degree < (rangeAngle / 2)) {
                // 判断实体和玩家之间是否有阻隔
                if (livingShooter.hasLineOfSight(victimEntity)) {
                    _doPerLivingHurt(livingShooter, victimEntity, knockback, realDamage.get(), targetEffects);
                }
            }
        }
    }
    private static void _doPerLivingHurt(LivingEntity livingShooter, LivingEntity victimEntity, float knockback, float damage, @Nullable List<_TargetEffectData> targetEffects) {
        // 不打自己
        if (livingShooter.equals(victimEntity)) return;

        @Nullable ServerLevel serverLevel = livingShooter.level() instanceof ServerLevel level ? level : null;
        DamageSource damageSource = livingShooter instanceof ServerPlayer serverPlayer ? livingShooter.damageSources().playerAttack(serverPlayer)
                : livingShooter.damageSources().mobAttack(livingShooter);
        victimEntity.knockback(knockback,
                (float) Math.sin(Math.toRadians(livingShooter.getYRot())),
                (float) -Math.cos(Math.toRadians(livingShooter.getYRot()))
        );
        if (serverLevel != null) victimEntity.hurt(damageSource, damage); // victimEntity.hurtServer(serverLevel, damageSource, damage);

        // 使近战枪械兼容神化词条/宝石
        if (serverLevel != null) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, victimEntity, damageSource);
        }

        if (!victimEntity.isAlive()) return;

        if (targetEffects != null) {
            IMcRegistry mcRegistry = CustomGun.getMcRegistry();
            for (_TargetEffectData targetEffectData : targetEffects) {
                var effectLocation = targetEffectData.getEffectLocation();
                var mobEffect = mcRegistry.getMobEffect_orHolder(effectLocation);
                if (mobEffect == null) continue;

                int effectTicks = Math.max(0, targetEffectData.getSeconds() * 20);
                int amplifier = Math.max(0, targetEffectData.getAmplifier());
                MobEffectInstance effectInstance = new MobEffectInstance(mobEffect, effectTicks, amplifier, false, targetEffectData.getHideParticles());
                victimEntity.addEffect(effectInstance);
            }
        }

        if (serverLevel != null) {
            int count = (int) (damage * 0.5);
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    victimEntity.getX(), victimEntity.getY(0.5), victimEntity.getZ(),
                    count,
                    0.1, 0, 0.1,
                    .2);
        }
    }
}
