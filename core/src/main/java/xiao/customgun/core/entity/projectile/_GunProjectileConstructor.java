/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.item.gun.modifier.*;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._BulletData;
import xiao.customgun.core.resource.data.data.gun.bullet._BulletSkillData;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;

public class _GunProjectileConstructor {

    /**
     * 构造各成员变量初值 (原模组构造函数那一大坨东西)
     * @param shooterGunModifierCache 射手枪械修饰缓存，直接生成的枪射物可不带此缓存
     */
    @ApiStatus.Internal
    protected static void constructInitData(GunProjectile _this, @Nullable ShooterGunModifierCache shooterGunModifierCache) {
        if (_this.gunIndexInstanceCache == null) {
            return;
        }

        // ----初值设置----

        _this.stateCache.shootPos = _this.position();
        GunData gunData = _this.gunIndexInstanceCache.getGunData();
        _BulletData bulletData = gunData.getBulletData();
        {
            _BulletSkillData bulletSkillData = bulletData.getBulletSkillData();
            _this.stateCache.armorIgnorePercent = bulletSkillData.getArmorIgnorePercent();
            _this.stateCache.headshotMultiplier = bulletSkillData.getHeadshotMultiplier();
            _this.stateCache.damageCalculation = bulletSkillData.getDamageCalculation();
        }
        _this.stateCache.lifetimeTicks = (int) (bulletData.getLifetimeSeconds() * 20);
        _this.stateCache.bulletSpeed = bulletData.getBulletSpeed();
        _this.stateCache.gravity = bulletData.getGravity();
        _this.stateCache.friction = bulletData.getFriction();
        _this.stateCache.pierce = bulletData.getPierceCount();
        int tracerInterval = bulletData.getTracerInterval();
        if (tracerInterval >= 0) {
            Entity owner = _this.getOwner();
            if (owner instanceof LivingEntity livingEntity) {
                _this.stateCache.isTracer = ILivingShooterGetter.cgc$fromLivingEntity(livingEntity).cgc$nextBulletIsTracer(tracerInterval);
            }
        }
        _this.stateCache.fireAspect = bulletData.isFireAspect();
        _this.stateCache.fireAspectSeconds = bulletData.getFireAspectSeconds();
        _this.stateCache.knockbackStrength = bulletData.getKnockbackStrength();
        boolean enableExplosion = false;
        {
            _ExplosionData explosionData = bulletData.getExplosionData();
            if (explosionData.getEnableExplode()) {
                enableExplosion = true;
                _this.stateCache.explosionData = explosionData;
            }
        }

        // ----射手枪械缓存----

        if (shooterGunModifierCache != null) {
            var armorIgnorePercent = IArmorIgnoreModifier.getValue(shooterGunModifierCache, AttachmentModifierType.ARMOR_IGNORE_PERCENT); if (armorIgnorePercent != null) _this.stateCache.armorIgnorePercent = armorIgnorePercent;
            var headshotMultiplier = IHeadshotMultiplierModifier.getValue(shooterGunModifierCache, AttachmentModifierType.HEADSHOT_MULTIPLIER); if (headshotMultiplier != null) _this.stateCache.headshotMultiplier = headshotMultiplier;
            var knockbackStrength = IKnockbackStrengthModifier.getValue(shooterGunModifierCache, AttachmentModifierType.KNOCKBACK_STRENGTH); if (knockbackStrength != null) _this.stateCache.knockbackStrength = knockbackStrength;
            var fireAspect = IFireAspectModifier.getValue(shooterGunModifierCache, AttachmentModifierType.FIRE_ASPECT); if (fireAspect != null) _this.stateCache.fireAspect = fireAspect;
            var damageCalculation = IDamageCalculationModifier.getValue(shooterGunModifierCache, AttachmentModifierType.DAMAGE_CALCULATION); if (damageCalculation != null) _this.stateCache.damageCalculation = damageCalculation;
            if (enableExplosion) {
                var bulletExplosion = IBulletExplosionModifier.getValue(shooterGunModifierCache, AttachmentModifierType.BULLET_EXPLOSION); if (bulletExplosion != null) _this.stateCache.explosionData = bulletExplosion;
            }
        }

        // ----脚本修改----

        applyScriptModification(_this, shooterGunModifierCache);
    }
    protected static void applyScriptModification(GunProjectile _this, @Nullable ShooterGunModifierCache shooterGunModifierCache) {
        if (!(_this.getOwner() instanceof LivingEntity livingShooter)) {
            return;
        }
        ItemStack gunItem = livingShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter);
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        if (!scriptApi.resetCache()) return;

        // TODO
    }
}
