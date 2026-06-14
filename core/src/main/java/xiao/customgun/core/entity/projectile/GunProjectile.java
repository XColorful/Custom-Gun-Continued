package xiao.customgun.core.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.projectile.GunProjectileDataAccessor;

/**
 * 枪射物 extends 投掷物
 */
public class GunProjectile extends Projectile implements IGunProjectile, GunProjectileDataAccessor {

    public GunProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
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
        this.setIsTracer(compoundTag, this.getIsTracer(this));
        this.setFireAspect(compoundTag, this.getFireAspect(this));
        this.setKnockbackStrength(compoundTag, this.getKnockbackStrength(this));
        if (this.hasExtraStateTag(this)) this.setExtraStateTag(compoundTag, this.getExtraStateTag(this));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
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
        this.setIsTracer(this, this.getIsTracer(compoundTag));
        this.setFireAspect(this, this.getFireAspect(compoundTag));
        this.setKnockbackStrength(this, this.getKnockbackStrength(compoundTag));
        @Nullable CompoundTag extraStateTag = this.getExtraStateTag(compoundTag); if (extraStateTag != null) this.setExtraStateTag(this, extraStateTag);
    }
}
