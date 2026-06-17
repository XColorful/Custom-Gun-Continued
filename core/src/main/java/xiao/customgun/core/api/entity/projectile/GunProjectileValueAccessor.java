package xiao.customgun.core.api.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.GunProjectileProperty;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.util.NBTUtils;

@ApiStatus.AvailableSince("1.21.6")
public interface GunProjectileValueAccessor extends IGunProjectileValueAccess {

    // --------IGunProjectileValueAccess--------

    @Override
    default @Nullable String getManagerGroupTag(ValueInput input) {
        return NBTUtils.Value.getString(input, GunProjectileProperty.MANAGER_GROUP.getTagName());
    }
    @Override
    default void setManagerGroupTag(ValueOutput output, String managerGroupTag) {
        NBTUtils.Value.setString(output, GunProjectileProperty.MANAGER_GROUP.getTagName(), managerGroupTag);
    }

    @Override
    default @NotNull Identifier getGunLocation(ValueInput input) {
        var gunLocation = NBTUtils.Value.getResourceLocation(input, GunProjectileProperty.GUN_LOCATION.getTagName());
        return gunLocation != null ? gunLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunLocation(ValueOutput output, Identifier gunLocation) {
        NBTUtils.Value.setResourceLocation(output, GunProjectileProperty.GUN_LOCATION.getTagName(), gunLocation);
    }

    @Override
    default @NotNull Identifier getGunDisplayLocation(ValueInput input) {
        var gunDisplayLocation = NBTUtils.Value.getResourceLocation(input, GunProjectileProperty.GUN_DISPLAY_LOCATION.getTagName());
        return gunDisplayLocation != null ? gunDisplayLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunDisplayLocation(ValueOutput output, Identifier gunDisplayLocation) {
        NBTUtils.Value.setResourceLocation(output, GunProjectileProperty.GUN_DISPLAY_LOCATION.getTagName(), gunDisplayLocation);
    }

    @Override
    default @NotNull Identifier getAmmoLocation(ValueInput input) {
        var ammoLocation = NBTUtils.Value.getResourceLocation(input, GunProjectileProperty.AMMO_LOCATION.getTagName());
        return ammoLocation != null ? ammoLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAmmoLocation(ValueOutput output, Identifier ammoLocation) {
        NBTUtils.Value.setResourceLocation(output, GunProjectileProperty.AMMO_LOCATION.getTagName(), ammoLocation);
    }

    @Override
    default boolean hasExtraDataTag(ValueInput input) {
        return NBTUtils.Value.hasKey(input, GunProjectileProperty.EXTRA_DATA.getTagName());
    }
    @Override
    default @Nullable CompoundTag getExtraDataTag(ValueInput input) {
        return NBTUtils.Value.getCompoundTag(input, GunProjectileProperty.EXTRA_DATA.getTagName());
    }
    @Override
    default void setExtraDataTag(ValueOutput output, CompoundTag extraDataTag) {
        NBTUtils.Value.setCompoundTag(output, GunProjectileProperty.EXTRA_DATA.getTagName(), extraDataTag);
    }

    // --------IGunProjectileStateAccess--------

    @Override
    default int getLifetimeTicks(ValueInput input) {
        return NBTUtils.Value.getInt(input, GunProjectileProperty.LIFETIME_TICKS.getTagName());
    }
    @Override
    default void setLifetimeTicks(ValueOutput output, int lifetimeTicks) {
        NBTUtils.Value.setInt(output, GunProjectileProperty.LIFETIME_TICKS.getTagName(), lifetimeTicks);
    }

    @Override
    default float getBulletSpeed(ValueInput input) {
        return NBTUtils.Value.getFloat(input, GunProjectileProperty.BULLET_SPEED.getTagName());
    }
    @Override
    default void setBulletSpeed(ValueOutput output, float bulletSpeed) {
        NBTUtils.Value.setFloat(output, GunProjectileProperty.BULLET_SPEED.getTagName(), bulletSpeed);
    }

    @Override
    default float getGravity(ValueInput input) {
        return NBTUtils.Value.getFloat(input, GunProjectileProperty.GRAVITY.getTagName());
    }
    @Override
    default void setGravity(ValueOutput output, float gravity) {
        NBTUtils.Value.setFloat(output, GunProjectileProperty.GRAVITY.getTagName(), gravity);
    }

    @Override
    default float getFriction(ValueInput input) {
        return NBTUtils.Value.getFloat(input, GunProjectileProperty.FRICTION.getTagName());
    }
    @Override
    default void setFriction(ValueOutput output, float friction) {
        NBTUtils.Value.setFloat(output, GunProjectileProperty.FRICTION.getTagName(), friction);
    }

    @Override
    default int getPierce(ValueInput input) {
        return NBTUtils.Value.getInt(input, GunProjectileProperty.PIERCE.getTagName());
    }
    @Override
    default void setPierce(ValueOutput output, int pierce) {
        NBTUtils.Value.setInt(output, GunProjectileProperty.PIERCE.getTagName(), pierce);
    }

    @Override
    default boolean getIsTracer(ValueInput input) {
        return NBTUtils.Value.getBoolean(input, GunProjectileProperty.IS_TRACER.getTagName());
    }
    @Override
    default void setIsTracer(ValueOutput output, boolean isTracer) {
        NBTUtils.Value.setBoolean(output, GunProjectileProperty.IS_TRACER.getTagName(), isTracer);
    }

    @Override
    default boolean getFireAspect(ValueInput input) {
        return NBTUtils.Value.getBoolean(input, GunProjectileProperty.FIRE_ASPECT.getTagName());
    }
    @Override
    default void setFireAspect(ValueOutput output, boolean fireAspect) {
        NBTUtils.Value.setBoolean(output, GunProjectileProperty.FIRE_ASPECT.getTagName(), fireAspect);
    }

    @Override
    default float getKnockbackStrength(ValueInput input) {
        return NBTUtils.Value.getFloat(input, GunProjectileProperty.KNOCKBACK_STRENGTH.getTagName());
    }
    @Override
    default void setKnockbackStrength(ValueOutput output, float knockbackStrength) {
        NBTUtils.Value.setFloat(output, GunProjectileProperty.KNOCKBACK_STRENGTH.getTagName(), knockbackStrength);
    }

    @Override
    default boolean hasExtraStateTag(ValueInput input) {
        return NBTUtils.Value.hasKey(input, GunProjectileProperty.EXTRA_STATE.getTagName());
    }
    @Override
    default @Nullable CompoundTag getExtraStateTag(ValueInput input) {
        return NBTUtils.Value.getCompoundTag(input, GunProjectileProperty.EXTRA_STATE.getTagName());
    }
    @Override
    default void setExtraStateTag(ValueOutput output, CompoundTag extraStateTag) {
        NBTUtils.Value.setCompoundTag(output, GunProjectileProperty.EXTRA_STATE.getTagName(), extraStateTag);
    }
}
