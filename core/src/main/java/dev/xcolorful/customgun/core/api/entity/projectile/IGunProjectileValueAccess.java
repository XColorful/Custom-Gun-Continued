package dev.xcolorful.customgun.core.api.entity.projectile;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.AvailableSince("1.21.6")
public interface IGunProjectileValueAccess {

    @Nullable String getManagerGroupTag(ValueInput input);
    void setManagerGroupTag(ValueOutput output, String managerGroupTag);

    /**
     * 获取枪械ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getGunLocation(ValueInput input);
    void setGunLocation(ValueOutput output, Identifier gunLocation);
    /**
     * 获取NBT指定的GunDisplay，如无则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getGunDisplayLocation(ValueInput input);
    void setGunDisplayLocation(ValueOutput output, Identifier gunDisplayLocation);
    /**
     * 获取子弹ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getAmmoLocation(ValueInput input);
    void setAmmoLocation(ValueOutput output, Identifier ammoLocation);

    boolean hasExtraDataTag(ValueInput input);
    @Nullable CompoundTag getExtraDataTag(ValueInput input);
    void setExtraDataTag(ValueOutput output, CompoundTag extraDataTag);

    // --------IGunProjectileStateAccess--------

    @Nullable Vec3 getShootPos(ValueInput input);
    void setShootPos(ValueOutput output, Vec3 shootPos);

    @Nullable List<_DistanceDamageData> getDamageCalculation(ValueInput input);
    void setDamageCalculation(ValueOutput output, List<_DistanceDamageData> damageCalculation);

    float getArmorIgnorePercent(ValueInput input);
    void setArmorIgnorePercent(ValueOutput output, float armorIgnorePercent);

    float getHeadshotMultiplier(ValueInput input);
    void setHeadshotMultiplier(ValueOutput output, float headshotMultiplier);

    int getLifetimeTicks(ValueInput input);
    void setLifetimeTicks(ValueOutput output, int lifetimeTicks);

    float getBulletSpeed(ValueInput input);
    void setBulletSpeed(ValueOutput output, float bulletSpeed);

    float getGravity(ValueInput input);
    void setGravity(ValueOutput output, float gravity);

    float getFriction(ValueInput input);
    void setFriction(ValueOutput output, float friction);

    int getPierce(ValueInput input);
    void setPierce(ValueOutput output, int pierce);

    boolean getIsTracer(ValueInput input);
    void setIsTracer(ValueOutput output, boolean isTracer);

    boolean getFireAspect(ValueInput input);
    void setFireAspect(ValueOutput output, boolean fireAspect);

    int getFireAspectSeconds(ValueInput input);
    void setFireAspectSeconds(ValueOutput output, int fireAspectSeconds);

    float getKnockbackStrength(ValueInput input);
    void setKnockbackStrength(ValueOutput output, float knockbackStrength);

    boolean hasExtraStateTag(ValueInput input);
    @Nullable CompoundTag getExtraStateTag(ValueInput input);
    void setExtraStateTag(ValueOutput output, CompoundTag extraStateTag);

    @Nullable _ExplosionData getExplosionData(ValueInput input);
    void setExplosionData(ValueOutput output, _ExplosionData explosionData);
}
