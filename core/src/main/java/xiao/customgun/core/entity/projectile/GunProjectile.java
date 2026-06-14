/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.gun.GunPropertyCache;
import xiao.customgun.core.api.entity.projectile.GunProjectileDataAccessor;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.config.AmmoConfig;
import xiao.customgun.core.config.SyncConfig;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import xiao.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 枪射物 extends 投掷物
 */
public class GunProjectile extends Projectile implements IGunProjectile, GunProjectileDataAccessor {

    private Vec3 spawnPos;

    // --------Cache--------
    private @Nullable GunIndexInstance gunIndexInstanceCache;
    private @Nullable AmmoIndexInstance ammoIndexInstanceCache;
    // --------Mixin--------
//    private @Nullable ClientGunIndexInstance cgc$clientGunIndexInstanceCache;
//    private @Nullable GunDisplayInstance cgc$gunDisplayInstanceCache;

    public GunProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
    public GunProjectile(EntityType<? extends Projectile> entityType, Level level,
                         @Nullable LivingEntity livingShooter,
                         ResourceLocation gunLocation, ResourceLocation gunDisplayLocation, ResourceLocation ammoLocation) {
        this(entityType, level);
        this.setOwner(livingShooter);
        this.setGunLocation(this, gunLocation);
        this.setGunDisplayLocation(this, gunDisplayLocation);
        this.setAmmoLocation(this, ammoLocation);
        this.spawnPos = this.position();

        @Nullable GunPropertyCache gunPropertyCache = livingShooter != null ? ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$getGunPropertyCache() : null;

        this.rebuildCache();
    }

    @Override
    protected void defineSynchedData() {
    }

    public void rebuildCache() {
        this.gunIndexInstanceCache = ResourceApi.getGunIndexInstance(this.getGunLocation(this));
        this.ammoIndexInstanceCache = ResourceApi.getAmmoIndexInstance(this.getAmmoLocation(this));
        // --------Mixin--------
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        @NotNull CompoundTag _this = NBTUtils.getOrCreateCustomData(this);
        // ----IGunProjectileDataAccess----
        this.setManagerGroupTag(compoundTag, this.getManagerGroupTag(_this));
        this.setGunLocation(compoundTag, this.getGunLocation(_this));
        this.setGunDisplayLocation(compoundTag, this.getGunDisplayLocation(_this));
        this.setAmmoLocation(compoundTag, this.getAmmoLocation(_this));
        if (this.hasExtraDataTag(this)) this.setExtraDataTag(compoundTag, this.getExtraDataTag(_this));
        // ----IGunProjectileStateAccess----
        this.setLifetimeTicks(compoundTag, this.getLifetimeTicks(_this));
        this.setBulletSpeed(compoundTag, this.getBulletSpeed(_this));
        this.setGravity(compoundTag, this.getGravity(_this));
        this.setFriction(compoundTag, this.getFriction(_this));
        this.setIsTracer(compoundTag, this.getIsTracer(_this));
        this.setFireAspect(compoundTag, this.getFireAspect(_this));
        this.setKnockbackStrength(compoundTag, this.getKnockbackStrength(_this));
        if (this.hasExtraStateTag(this)) this.setExtraStateTag(compoundTag, this.getExtraStateTag(_this));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        @NotNull CompoundTag _this = NBTUtils.getOrCreateCustomData(this);
        // ----IGunProjectileDataAccess----
        this.setManagerGroupTag(_this, this.getManagerGroupTag(compoundTag));
        this.setGunLocation(_this, this.getGunLocation(compoundTag));
        this.setGunDisplayLocation(_this, this.getGunDisplayLocation(compoundTag));
        this.setAmmoLocation(_this, this.getAmmoLocation(compoundTag));
        @Nullable CompoundTag extraDataTag = this.getExtraDataTag(compoundTag); if (extraDataTag != null) this.setExtraDataTag(_this, extraDataTag);
        // ----IGunProjectileStateAccess----
        this.setLifetimeTicks(_this, this.getLifetimeTicks(compoundTag));
        this.setBulletSpeed(_this, this.getBulletSpeed(compoundTag));
        this.setGravity(_this, this.getGravity(compoundTag));
        this.setFriction(_this, this.getFriction(compoundTag));
        this.setIsTracer(_this, this.getIsTracer(compoundTag));
        this.setFireAspect(_this, this.getFireAspect(compoundTag));
        this.setKnockbackStrength(_this, this.getKnockbackStrength(compoundTag));
        @Nullable CompoundTag extraStateTag = this.getExtraStateTag(compoundTag); if (extraStateTag != null) this.setExtraStateTag(_this, extraStateTag);

        this.rebuildCache();
    }

    // --------IGunProjectile--------

    @Override
    public @Nullable GunIndexInstance getGunIndexInstanceCache() {
        return this.gunIndexInstanceCache;
    }
    @Override
    public @Nullable AmmoIndexInstance getAmmoIndexInstanceCache() {
        return this.ammoIndexInstanceCache;
    }

    @Override
    public void setGunIndexInstanceCache(GunIndexInstance cache) {
        this.gunIndexInstanceCache = cache;
    }
    @Override
    public void setAmmoIndexInstanceCache(AmmoIndexInstance cache) {
        this.ammoIndexInstanceCache = cache;
    }

    // --------Deprecated--------
    // 原字段的获取方式

    @Deprecated public ResourceLocation ammoId() {
        return this.getAmmoLocation(this);
    }
    @Deprecated public float speed() {
        return this.getBulletSpeed(this);
    }
    @Deprecated public float gravity() {
        return this.getGravity(this);
    }
    @Deprecated public float friction() {
        return this.getFriction(this);
    }
    @Deprecated public List<_DistanceDamageData> damageAmount() {
        if (this.gunIndexInstanceCache == null) return new ArrayList<>();
        return this.gunIndexInstanceCache.getGunData().getBulletData().getBulletSkillData().getDamageCalculation();
    }
    @Deprecated public float knockback() {
        if (this.gunIndexInstanceCache == null) return 0;
        return this.gunIndexInstanceCache.getGunData().getBulletData().getKnockbackStrength();
    }
    @Deprecated public float distanceAmount(@Nullable GunPropertyCache gunPropertyCache) {
        if (gunPropertyCache == null) return 0;
//        return gunPropertyCache.getCache("effective_range");
        return 0;
    }
    @Deprecated public @Nullable _ExplosionData explosionData() {
        if (this.gunIndexInstanceCache == null) return null;
        return this.gunIndexInstanceCache.getGunData().getBulletData().getExplosionData();
    }
    @Deprecated public boolean explosion() {
        _ExplosionData explosionData = explosionData();
        return explosionData != null && explosionData.getEnableExplode();
    }
    @Deprecated public float explosionDamage() {
        _ExplosionData explosionData = explosionData();
        return explosionData != null ? (float) (explosionData.getExplodeDamage() * SyncConfig.DAMAGE_BASE_MULTIPLIER.get()) : 0;
    }
    @Deprecated public float explosionRadius() {
        _ExplosionData explosionData = explosionData();
        return explosionData != null ? explosionData.getExplodeScale() : 0;
    }
    @Deprecated public boolean explosionKnockback() {
        _ExplosionData explosionData = explosionData();
        return explosionData != null && explosionData.getEnableKnockback();
    }
    @Deprecated public boolean explosionDestroyBlock() {
        _ExplosionData explosionData = explosionData();
        return explosionData != null && explosionData.getEnableWorldDestruction() && AmmoConfig.EXPLOSIVE_AMMO_DESTROYS_BLOCK.get();
    }
    @Deprecated public int explosionDelayCount() {
        _ExplosionData explosionData = explosionData();
        return explosionData != null ? (int) (explosionData.getMaxDelaySeconds() * 20) : 0;
    }
    @Deprecated public boolean igniteEntity() {
        if (this.gunIndexInstanceCache == null) return false;
        return this.gunIndexInstanceCache.getGunData().getBulletData().isFireAspect();
    }
    @Deprecated public int igniteEntityTime() {
        if (this.gunIndexInstanceCache == null) return 0;
        return this.gunIndexInstanceCache.getGunData().getBulletData().getFireAspectSeconds();
    }
    @Deprecated public float damageModifier() {
        if (this.gunIndexInstanceCache == null) return 0;
        return 1f / this.gunIndexInstanceCache.getGunData().getBulletData().getBulletAmount();
    }
    @Deprecated public int pierce(@Nullable GunPropertyCache gunPropertyCache) {
        if (gunPropertyCache == null) return 0;
//        return gunPropertyCache.getCache("pierce");
        return 0;
    }
    @Deprecated public boolean isTracerAmmo() {
        return this.getIsTracer(this);
    }
    @Deprecated public ResourceLocation gunId() {
        return this.getGunLocation(this);
    }
    @Deprecated public ResourceLocation gunDisplayId() {
        return this.getGunDisplayLocation(this);
    }
    @Deprecated public float getArmorIgnore(@Nullable GunPropertyCache gunPropertyCache) {
        if (this.gunIndexInstanceCache == null) return 0;
//        return this.gunIndexInstanceCache.getGunData().getBulletData().getBulletSkillData().getArmorIgnorePercent();
//        return gunPropertyCache.getCache("armor_ignore");
        return 0;
    }
    @Deprecated public float headshot() {
        if (this.gunIndexInstanceCache == null) return 1;
        return this.gunIndexInstanceCache.getGunData().getBulletData().getBulletSkillData().getHeadshotMultiplier();
    }
    @Deprecated public float shotDamageMultiplier() {
        return 0; // 源码没有调用过ScriptAPI的setter
    }
}
