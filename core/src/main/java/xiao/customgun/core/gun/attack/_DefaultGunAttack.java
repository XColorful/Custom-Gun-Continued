/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.attack;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.gun.attack.IGunAttackRuntime;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.item.gun.GunDataAccessor;
import xiao.customgun.core.api.item.gun.MeleeType;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._MeleeModifierData;
import xiao.customgun.core.resource.data.data.attachment.melee._TargetEffectData;
import xiao.customgun.core.resource.data.data.gun._MeleeData;
import xiao.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.List;
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
            switch (boltType) {
                case MANUAL_ACTION -> {
                    // 检查枪管是否有子弹 (否则要拉栓)
                    if (!iGun.hasBarrelAmmo(gunItem)) {
                        return IGunAttackRuntime.ShooterFireResult.NO_BARREL_AMMO;
                    }
                }
                case CLOSED_BOLT -> {
                    // 检查枪管是否有子弹 (否则要上膛)
                    if (!iGun.hasBarrelAmmo(gunItem)) {
                        // 已经有子弹，仅在服务端执行NBT换弹逻辑
                        if (!logicalSide.isClient()) {
                            if (iGun.useInventoryAmmo(gunItem)) _consumeAmmoFromPlayer(iGun, gunItem, iLivingShooter, livingShooter);
                            else iGun.consumeMagAmmo(gunItem);

                            if (PlannedRefactor.ON_SET_BARREL_AMMO) {};
                            iGun.setBarrelAmmoCount(gunItem, 1);
                        }
                    }
                }
            }
        }

        // 客户端侧提前返回，以继续客户端逻辑
        if (logicalSide.isClient()) return IGunAttackRuntime.ShooterFireResult.SUCCESS;

        // --------服务端--------

        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        { // 4. 蓄力进度检查
            if (!_DefaultGunCharge.isChargeProgressAcceptable(shooterProperty, gunData, fireModeType, clientChargeProgress)) {
                return IGunAttackRuntime.ShooterFireResult.NOT_CHARGED;
            }
        }

        return IGunAttackRuntime.ShooterFireResult.SUCCESS;
    }

    private static void _consumeAmmoFromPlayer(IGun iGun, ItemStack gunItem,
                                               ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        if (!iLivingShooter.cgc$needCheckAmmo()) return;

        if (iGun.useDummyAmmo(gunItem)) {
            // TODO 这个逻辑是要统一在consumeAmmoOnce里处理的
            iGun.findAndExtractDummyAmmo(iGun, gunItem, 1);
        } else {
            IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
            iGun.findAndExtractInventoryAmmo(inventoryCapability, iGun, gunItem, 1);
        }
    }

    /**
     * 对应原模组{@code AbstractGunItem.shoot()}，仅服务端触发
     */
    protected static IGunAttackRuntime.GunFireResult gunFire(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                             ILivingShooter iLivingShooter, LivingEntity livingShooter) {

        return IGunAttackRuntime.GunFireResult.SUCCESS;
    }

    protected static void doBulletSpread(ShooterProperty shooterProperty,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                         ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                         @NotNull IGunProjectile iGunProjectile, @NotNull Projectile projectile,
                                         int bulletId,
                                         float xRot, float yRot, float pow, float uncertainty) {
        float yOffset = 0;
        projectile.shootFromRotation(livingShooter, xRot, yRot, yOffset, pow, uncertainty);
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

    protected static void melee(ShooterProperty shooterProperty,
                                @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                MeleeType meleeType) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        GunData gunData = gunIndexInstance.getGunData();
        _MeleeData meleeData = gunData.getMeleeData();
        float gunBaseLength = meleeData.getGunBaseLength();

        switch (meleeType) {
            case BAYONET -> _doAttachmentMelee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter,
                    gunBaseLength, AttachmentCategory.MUZZLE);
            case STOCK -> _doAttachmentMelee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter,
                    gunBaseLength, AttachmentCategory.STOCK);
            case PUSH -> {
                _DefaultMeleeData defaultMeleeData = meleeData.getDefaultMeleeData();
                _doGunMelee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter,
                    gunBaseLength, defaultMeleeData);
            }
            // 增加类型使此处强制编译不通过
        }
    }
    private static void _doAttachmentMelee(ShooterProperty shooterProperty,
                                              @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                              ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                              float gunBaseLength, AttachmentCategory attachmentCategory) {
        @Nullable _MeleeModifierData meleeModifierData = GunDataAccessor._getAttachmentMeleeModifierData(iGun, gunItem, attachmentCategory);
        if (meleeModifierData == null) return;

        _doMelee(iLivingShooter, livingShooter,
                gunBaseLength,
                meleeModifierData.getMeleeDistance(), meleeModifierData.getRangeAngle(), meleeModifierData.getKnockbackStrength(), meleeModifierData.getMeleeDamage(), meleeModifierData.getTargetEffect());
    }
    private static void _doGunMelee(ShooterProperty shooterProperty,
                                       @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                       ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                       float gunBaseLength, _DefaultMeleeData defaultMeleeData) {
        _doMelee(iLivingShooter, livingShooter,
                gunBaseLength,
                defaultMeleeData.getMeleeDistance(), defaultMeleeData.getRangeAngle(), defaultMeleeData.getKnockbackStrength(), defaultMeleeData.getMeleeDamage(), null);
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

        victimEntity.knockback(knockback, (float) Math.sin(Math.toRadians(livingShooter.getYRot())), (float) -Math.cos(Math.toRadians(livingShooter.getYRot())));
        if (livingShooter instanceof ServerPlayer serverPlayer) {
            victimEntity.hurt(livingShooter.damageSources().playerAttack(serverPlayer), damage);
        } else {
            victimEntity.hurt(livingShooter.damageSources().mobAttack(livingShooter), damage);
        }

        // 使近战枪械兼容神化词条/宝石
        livingShooter.doEnchantDamageEffects(livingShooter, victimEntity);

        if (!victimEntity.isAlive()) return;

        if (targetEffects != null) {
            IMcRegistry mcRegistry = CustomGun.getMcRegistry();
            for (_TargetEffectData targetEffectData : targetEffects) {
                var effectLocation = targetEffectData.getEffectLocation();
                MobEffect mobEffect = mcRegistry.getMobEffect(effectLocation);
                if (mobEffect == null) continue;

                int effectTicks = Math.max(0, targetEffectData.getSeconds() * 20);
                int amplifier = Math.max(0, targetEffectData.getAmplifier());
                MobEffectInstance effectInstance = new MobEffectInstance(mobEffect, effectTicks, amplifier, false, targetEffectData.getHideParticles());
                victimEntity.addEffect(effectInstance);
            }
        }

        if (livingShooter.level() instanceof ServerLevel serverLevel) {
            int count = (int) (damage * 0.5);
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    victimEntity.getX(), victimEntity.getY(0.5), victimEntity.getZ(),
                    count,
                    0.1, 0, 0.1,
                    .2);
        }
    }
}
