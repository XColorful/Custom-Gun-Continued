package dev.xcolorful.customgun.core.api.entity.projectile;

import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IGunProjectileStateAccess {

    @Nullable Vec3 getShootPos(Entity gunProjectile);
    void setShootPos(Entity gunProjectile, Vec3 shootPos);

    float getArmorIgnorePercent(Entity gunProjectile);
    void setArmorIgnorePercent(Entity gunProjectile, float armorIgnorePercent);

    float getHeadshotMultiplier(Entity gunProjectile);
    void setHeadshotMultiplier(Entity gunProjectile, float headshotMultiplier);

    @Nullable List<_DistanceDamageData> getDamageCalculation(Entity gunProjectile);
    void setDamageCalculation(Entity gunProjectile, List<_DistanceDamageData> damageCalculation);

    int getLifetimeTicks(Entity gunProjectile);
    void setLifetimeTicks(Entity gunProjectile, int lifetimeTicks);

    float getBulletSpeed(Entity gunProjectile);
    void setBulletSpeed(Entity gunProjectile, float bulletSpeed);

    float getGravity(Entity gunProjectile);
    void setGravity(Entity gunProjectile, float gravity);

    float getFriction(Entity gunProjectile);
    void setFriction(Entity gunProjectile, float friction);

    int getPierce(Entity gunProjectile);
    void setPierce(Entity gunProjectile, int pierce);

    boolean getIsTracer(Entity gunProjectile);
    void setIsTracer(Entity gunProjectile, boolean isTracer);

    boolean getFireAspect(Entity gunProjectile);
    void setFireAspect(Entity gunProjectile, boolean fireAspect);

    int getFireAspectSeconds(Entity gunProjectile);
    void setFireAspectSeconds(Entity gunProjectile, int fireAspectSeconds);

    float getKnockbackStrength(Entity gunProjectile);
    void setKnockbackStrength(Entity gunProjectile, float knockbackStrength);

    boolean hasExtraStateTag(Entity gunProjectile);
    @Nullable CompoundTag getExtraStateTag(Entity gunProjectile);
    void setExtraStateTag(Entity gunProjectile, CompoundTag extraStateTag);
}