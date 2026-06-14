package xiao.customgun.core.api.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IGunProjectileStateAccess {

    int getLifetimeTicks(Entity gunProjectile);
    void setLifetimeTicks(Entity gunProjectile, int lifetimeTicks);

    float getBulletSpeed(Entity gunProjectile);
    void setBulletSpeed(Entity gunProjectile, float bulletSpeed);

    float getGravity(Entity gunProjectile);
    void setGravity(Entity gunProjectile, float gravity);

    float getFriction(Entity gunProjectile);
    void setFriction(Entity gunProjectile, float friction);

    boolean getIsTracer(Entity gunProjectile);
    void setIsTracer(Entity gunProjectile, boolean isTracer);

    boolean getFireAspect(Entity gunProjectile);
    void setFireAspect(Entity gunProjectile, boolean fireAspect);

    float getKnockbackStrength(Entity gunProjectile);
    void setKnockbackStrength(Entity gunProjectile, float knockbackStrength);

    boolean hasExtraStateTag(Entity gunProjectile);
    @Nullable CompoundTag getExtraStateTag(Entity gunProjectile);
    void setExtraStateTag(Entity gunProjectile, CompoundTag extraStateTag);
}