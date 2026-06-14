package xiao.customgun.core.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.projectile.GunProjectileDataAccessor;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.NBTUtils;

/**
 * 枪射物 extends 投掷物
 */
public class GunProjectile extends Projectile implements IGunProjectile, GunProjectileDataAccessor {

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
}
