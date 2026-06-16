package xiao.customgun.core.api.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IGunProjectileNBTAccess {

    @Nullable String getManagerGroupTag(CompoundTag gunProjectileCustomDataTag);
    void setManagerGroupTag(CompoundTag gunProjectileCustomDataTag, String managerGroupTag);

    /**
     * 获取枪械ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getGunLocation(CompoundTag gunProjectileCustomDataTag);
    void setGunLocation(CompoundTag gunProjectileCustomDataTag, ResourceLocation gunLocation);
    /**
     * 获取NBT指定的GunDisplay，如无则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getGunDisplayLocation(CompoundTag gunProjectileCustomDataTag);
    void setGunDisplayLocation(CompoundTag gunProjectileCustomDataTag, ResourceLocation gunDisplayLocation);
    /**
     * 获取子弹ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getAmmoLocation(CompoundTag gunProjectileCustomDataTag);
    void setAmmoLocation(CompoundTag gunProjectileCustomDataTag, ResourceLocation ammoLocation);

    boolean hasExtraDataTag(CompoundTag gunProjectileCustomDataTag);
    @Nullable CompoundTag getExtraDataTag(CompoundTag gunProjectileCustomDataTag);
    void setExtraDataTag(CompoundTag gunProjectileCustomDataTag, CompoundTag extraDataTag);

    // --------IGunProjectileStateAccess--------

    int getLifetimeTicks(CompoundTag gunProjectileCustomDataTag);
    void setLifetimeTicks(CompoundTag gunProjectileCustomDataTag, int lifetimeTicks);

    float getBulletSpeed(CompoundTag gunProjectileCustomDataTag);
    void setBulletSpeed(CompoundTag gunProjectileCustomDataTag, float bulletSpeed);

    float getGravity(CompoundTag gunProjectileCustomDataTag);
    void setGravity(CompoundTag gunProjectileCustomDataTag, float gravity);

    float getFriction(CompoundTag gunProjectileCustomDataTag);
    void setFriction(CompoundTag gunProjectileCustomDataTag, float friction);

    int getPierce(CompoundTag gunProjectileCustomDataTag);
    void setPierce(CompoundTag gunProjectileCustomDataTag, int pierce);

    boolean getIsTracer(CompoundTag gunProjectileCustomDataTag);
    void setIsTracer(CompoundTag gunProjectileCustomDataTag, boolean isTracer);

    boolean getFireAspect(CompoundTag gunProjectileCustomDataTag);
    void setFireAspect(CompoundTag gunProjectileCustomDataTag, boolean fireAspect);

    float getKnockbackStrength(CompoundTag gunProjectileCustomDataTag);
    void setKnockbackStrength(CompoundTag gunProjectileCustomDataTag, float knockbackStrength);

    boolean hasExtraStateTag(CompoundTag gunProjectileCustomDataTag);
    @Nullable CompoundTag getExtraStateTag(CompoundTag gunProjectileCustomDataTag);
    void setExtraStateTag(CompoundTag gunProjectileCustomDataTag, CompoundTag extraStateTag);
}
