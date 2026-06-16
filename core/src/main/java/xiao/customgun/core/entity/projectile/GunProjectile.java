/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.config.AmmoConfig;
import xiao.customgun.core.config.SyncConfig;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.projectile.ProjectileManager;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._BulletData;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import xiao.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * 枪射物 extends 投掷物
 */
public class GunProjectile extends Projectile implements IGunProjectile, GunProjectileDataAccessor {

    protected Vec3 spawnPos;
    protected final DataCache dataCache = new DataCache();
    protected final StateCache stateCache = new StateCache();

    // --------Cache--------
    protected @Nullable GunIndexInstance gunIndexInstanceCache;
    protected @Nullable AmmoIndexInstance ammoIndexInstanceCache;
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
        this.constructInitData();
    }

    @Override
    protected void defineSynchedData() {
    }

    protected void constructInitData() {
        if (this.gunIndexInstanceCache == null) {
            return;
        }

        GunData gunData = this.gunIndexInstanceCache.getGunData();
        _BulletData bulletData = gunData.getBulletData();
        this.stateCache.lifetimeTicks = (int) (bulletData.getLifetimeSeconds() * 20);
        this.stateCache.bulletSpeed = bulletData.getBulletSpeed();
        this.stateCache.gravity = bulletData.getGravity();
        this.stateCache.friction = bulletData.getFriction();
        this.stateCache.pierce = bulletData.getPierceCount();
        int tracerInterval = bulletData.getTracerInterval();
        if (tracerInterval >= 0) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingEntity) {
                this.stateCache.isTracer = ILivingShooterGetter.cgc$fromLivingEntity(livingEntity).cgc$nextBulletIsTracer(tracerInterval);
            }
        }
        this.stateCache.fireAspect = bulletData.isFireAspect();
        this.stateCache.knockbackStrength = bulletData.getKnockbackStrength();
    }

    public void rebuildCache() {
        this.gunIndexInstanceCache = ResourceApi.getGunIndexInstance(this.getGunLocation(this));
        this.ammoIndexInstanceCache = ResourceApi.getAmmoIndexInstance(this.getAmmoLocation(this));
        // --------Mixin--------
    }

    @Override
    public void tick() {
        super.tick();

        ProjectileManagerGroup group = ProjectileManager.INSTANCE.getProjectileManagerGroup(this.getManagerGroupTag(this));
        TickContext tickContext = new TickContext(group);

        if (PlannedRefactor.ON_PROJECTILE_TICK_EVENT) {
            return;
        }

        this.processTick(tickContext, this, this);

        if (PlannedRefactor.ON_PROJECTILE_TICK_FINISH_EVENT) {
            return;
        }
    }

    /**
     * 优化：原版机制在创建实体后，会先调用 {@link #addAdditionalSaveData}，再调用 {@link #readAdditionalSaveData}
     * 扩展模组如要手动调用，则先使用 {@link #addAdditionalSaveData} 刷新NBT数据
     */
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        // ----IGunProjectileDataAccess----
        this.setManagerGroupTag(compoundTag, this.getManagerGroupTag(this));
        this.setGunLocation(compoundTag, this.getGunLocation(this));
        this.setGunDisplayLocation(compoundTag, this.getGunDisplayLocation(this));
        this.setAmmoLocation(compoundTag, this.getAmmoLocation(this));
        if (this.hasExtraDataTag(this)) this.setExtraDataTag(compoundTag, this.getExtraDataTag(this));
        // ----IGunProjectileStateAccess----
        this.setLifetimeTicks(compoundTag, this.getLifetimeTicks(this));
        this.setBulletSpeed(compoundTag, this.getBulletSpeed(this));
        this.setGravity(compoundTag, this.getGravity(this));
        this.setFriction(compoundTag, this.getFriction(this));
        this.setPierce(compoundTag, this.getPierce(this));
        this.setIsTracer(compoundTag, this.getIsTracer(this));
        this.setFireAspect(compoundTag, this.getFireAspect(this));
        this.setKnockbackStrength(compoundTag, this.getKnockbackStrength(this));
        if (this.hasExtraStateTag(this)) this.setExtraStateTag(compoundTag, this.getExtraStateTag(this));
    }
    /**
     * 手动调用前请用 {@link #addAdditionalSaveData} 刷新NBT数据，并向返回的NBT里修改数据，再调用该方法
     */
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        // ----IGunProjectileDataAccess----
        this.setManagerGroupTag(this, this.getManagerGroupTag(compoundTag));
        this.setGunLocation(this, this.getGunLocation(compoundTag));
        this.setGunDisplayLocation(this, this.getGunDisplayLocation(compoundTag));
        this.setAmmoLocation(this, this.getAmmoLocation(compoundTag));
        @Nullable CompoundTag extraDataTag = this.getExtraDataTag(compoundTag); if (extraDataTag != null) this.setExtraDataTag(this, extraDataTag);
        // ----IGunProjectileStateAccess----
        this.setLifetimeTicks(this, this.getLifetimeTicks(compoundTag));
        this.setBulletSpeed(this, this.getBulletSpeed(compoundTag));
        this.setGravity(this, this.getGravity(compoundTag));
        this.setFriction(this, this.getFriction(compoundTag));
        this.setPierce(this, this.getPierce(compoundTag));
        this.setIsTracer(this, this.getIsTracer(compoundTag));
        this.setFireAspect(this, this.getFireAspect(compoundTag));
        this.setKnockbackStrength(this, this.getKnockbackStrength(compoundTag));
        @Nullable CompoundTag extraStateTag = this.getExtraStateTag(compoundTag); if (extraStateTag != null) this.setExtraStateTag(this, extraStateTag);

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

    // --------IGunProjectileDataAccess--------
    // 对实体的字段操作改为内存操作
    // 但保留写入指定NBT的操作 (IGunProjectileNBTAccess)

    @Override public @Nullable String getManagerGroupTag(Entity gunProjectile) {
        return this.dataCache.managerGroupTag;
    }
    @Override public void setManagerGroupTag(Entity entity, String managerGroupTag) {
        this.dataCache.managerGroupTag = managerGroupTag;
    }
    @Override public @NotNull ResourceLocation getGunLocation(Entity gunProjectile) {
        var gunLocation = this.dataCache.gunLocation;
        return gunLocation != null ? gunLocation : ResourceTag.NULL_LOCATION;
    }
    @Override public void setGunLocation(Entity entity, ResourceLocation gunLocation) {
        this.dataCache.gunLocation = gunLocation;
    }
    @Override public @NotNull ResourceLocation getGunDisplayLocation(Entity gunProjectile) {
        var gunDisplayLocation = this.dataCache.gunDisplayLocation;
        return gunDisplayLocation != null ? gunDisplayLocation : ResourceTag.NULL_LOCATION;
    }
    @Override public void setGunDisplayLocation(Entity entity, ResourceLocation gunDisplayLocation) {
        this.dataCache.gunDisplayLocation = gunDisplayLocation;
    }
    @Override public @NotNull ResourceLocation getAmmoLocation(Entity gunProjectile) {
        var ammoLocation = this.dataCache.ammoLocation;
        return ammoLocation != null ? ammoLocation : ResourceTag.NULL_LOCATION;
    }
    @Override public void setAmmoLocation(Entity entity, ResourceLocation ammoLocation) {
        this.dataCache.ammoLocation = ammoLocation;
    }
    @Override public boolean hasExtraDataTag(Entity gunProjectile) {
        return this.dataCache.extraDataTag != null;
    }
    @Override public @Nullable CompoundTag getExtraDataTag(Entity gunProjectile) {
        return this.dataCache.extraDataTag;
    }
    @Override public void setExtraDataTag(Entity entity, CompoundTag extraDataTag) {
        this.dataCache.extraDataTag = extraDataTag;
    }

    // --------IGunProjectileStateAccess--------
    // 对实体的字段操作改为内存操作
    // 但保留写入指定NBT的操作 (IGunProjectileNBTAccess)

    @Override public int getLifetimeTicks(Entity gunProjectile) {
        return this.stateCache.lifetimeTicks;
    }
    @Override public void setLifetimeTicks(Entity entity, int lifetimeTicks) {
        this.stateCache.lifetimeTicks = lifetimeTicks;
    }
    @Override public float getBulletSpeed(Entity gunProjectile) {
        return this.stateCache.bulletSpeed;
    }
    @Override public void setBulletSpeed(Entity entity, float bulletSpeed) {
        this.stateCache.bulletSpeed = bulletSpeed;
    }
    @Override public float getGravity(Entity gunProjectile) {
        return this.stateCache.gravity;
    }
    @Override public void setGravity(Entity entity, float gravity) {
        this.stateCache.gravity = gravity;
    }
    @Override public float getFriction(Entity gunProjectile) {
        return this.stateCache.friction;
    }
    @Override public void setFriction(Entity entity, float friction) {
        this.stateCache.friction = friction;
    }
    @Override public int getPierce(Entity gunProjectile) {
        return this.stateCache.pierce;
    }
    @Override public void setPierce(Entity gunProjectile, int pierce) {
        this.stateCache.pierce = pierce;
    }
    @Override public boolean getIsTracer(Entity gunProjectile) {
        return this.stateCache.isTracer;
    }
    @Override public void setIsTracer(Entity entity, boolean isTracer) {
        this.stateCache.isTracer = isTracer;
    }
    @Override public boolean getFireAspect(Entity gunProjectile) {
        return this.stateCache.fireAspect;
    }
    @Override public void setFireAspect(Entity entity, boolean fireAspect) {
        this.stateCache.fireAspect = fireAspect;
    }
    @Override public float getKnockbackStrength(Entity gunProjectile) {
        return this.stateCache.knockbackStrength;
    }
    @Override public void setKnockbackStrength(Entity entity, float knockbackStrength) {
        this.stateCache.knockbackStrength = knockbackStrength;
    }
    @Override public boolean hasExtraStateTag(Entity gunProjectile) {
        return this.stateCache.extraStateTag != null;
    }
    @Override public @Nullable CompoundTag getExtraStateTag(Entity gunProjectile) {
        return this.stateCache.extraStateTag;
    }
    @Override public void setExtraStateTag(Entity entity, CompoundTag extraStateTag) {
        this.stateCache.extraStateTag = extraStateTag;
    }

    // --------IProjectileRuntime--------

    // --------IProjectileEffectRuntime--------

    @Override public void impactEffect(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectileEffectManager().impactEffect(tickContext, iGunProjectile, gunProjectile);
    }
    @Override public void moveEffect(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectileEffectManager().impactEffect(tickContext, iGunProjectile, gunProjectile);
    }

    // --------IProjectileImpactRuntime--------

    @Override public void preImpactTick(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectileImpactManager().preImpactTick(tickContext, iGunProjectile, gunProjectile);
    }
    @Override public void impactTick(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectileImpactManager().impactTick(tickContext, iGunProjectile, gunProjectile);
    }

    // --------IProjectilePhysicsRuntime--------

    @Override public void physicTick(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectilePhysicsManager().physicTick(tickContext, iGunProjectile, gunProjectile);
    }
    @Override public void physicMove(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectilePhysicsManager().physicMove(tickContext, iGunProjectile, gunProjectile);
    }

    // --------IProjectileProcessRuntime--------

    @Override public void processTick(TickContext tickContext, IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.group.projectileProcessManager().processTick(tickContext, iGunProjectile, gunProjectile);
    }

    // --------偷渡--------

    /**
     * 偷渡方法 {@link Projectile#lerpRotation(float, float)}
     */
    public static float lerpRotation(float rotO, float rot) {
        return Projectile.lerpRotation(rotO, rot);
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
