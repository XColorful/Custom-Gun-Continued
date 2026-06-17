package xiao.customgun.core.api.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

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

    float getKnockbackStrength(ValueInput input);
    void setKnockbackStrength(ValueOutput output, float knockbackStrength);

    boolean hasExtraStateTag(ValueInput input);
    @Nullable CompoundTag getExtraStateTag(ValueInput input);
    void setExtraStateTag(ValueOutput output, CompoundTag extraStateTag);
}
