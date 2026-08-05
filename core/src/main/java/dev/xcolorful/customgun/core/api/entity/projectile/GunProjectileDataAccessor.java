package dev.xcolorful.customgun.core.api.entity.projectile;

import dev.xcolorful.customgun.core.api.entity.GunProjectileProperty;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import dev.xcolorful.customgun.core.util.JsonUtils;
import dev.xcolorful.customgun.core.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GunProjectileDataAccessor extends GunProjectileNBTAccessor, IGunProjectileDataAccess {

    // --------IGunProjectileDataAccess--------

    @Override
    default @Nullable String getManagerGroupTag(Entity gunProjectile) {
        return NBTUtils.getString(gunProjectile, GunProjectileProperty.MANAGER_GROUP.getTagName());
    }
    @Override
    default void setManagerGroupTag(Entity gunProjectile, String managerGroupTag) {
        NBTUtils.setString(gunProjectile, GunProjectileProperty.MANAGER_GROUP.getTagName(), managerGroupTag);
    }

    @Override
    default @NotNull ResourceLocation getGunLocation(Entity gunProjectile) {
        var gunLocation = NBTUtils.getResourceLocation(gunProjectile, GunProjectileProperty.GUN_LOCATION.getTagName());
        return gunLocation != null ? gunLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunLocation(Entity gunProjectile, ResourceLocation gunLocation) {
        NBTUtils.setResourceLocation(gunProjectile, GunProjectileProperty.GUN_LOCATION.getTagName(), gunLocation);
    }
    @Override
    default @NotNull ResourceLocation getGunDisplayLocation(Entity gunProjectile) {
        var gunDisplayLocation = NBTUtils.getResourceLocation(gunProjectile, GunProjectileProperty.GUN_DISPLAY_LOCATION.getTagName());
        return gunDisplayLocation != null ? gunDisplayLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunDisplayLocation(Entity gunProjectile, ResourceLocation gunDisplayLocation) {
        NBTUtils.setResourceLocation(gunProjectile, GunProjectileProperty.GUN_DISPLAY_LOCATION.getTagName(), gunDisplayLocation);
    }
    @Override
    default @NotNull ResourceLocation getAmmoLocation(Entity gunProjectile) {
        var ammoLocation = NBTUtils.getResourceLocation(gunProjectile, GunProjectileProperty.AMMO_LOCATION.getTagName());
        return ammoLocation != null ? ammoLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAmmoLocation(Entity gunProjectile, ResourceLocation ammoLocation) {
        NBTUtils.setResourceLocation(gunProjectile, GunProjectileProperty.AMMO_LOCATION.getTagName(), ammoLocation);
    }

    @Override
    default boolean hasExtraDataTag(Entity gunProjectile) {
        return NBTUtils.hasKey(gunProjectile, GunProjectileProperty.EXTRA_DATA.getTagName());
    }
    @Override
    default @Nullable CompoundTag getExtraDataTag(Entity gunProjectile) {
        return NBTUtils.getCompoundTag(gunProjectile, GunProjectileProperty.EXTRA_DATA.getTagName());
    }
    @Override
    default void setExtraDataTag(Entity gunProjectile, CompoundTag extraDataTag) {
        NBTUtils.setCompoundTag(gunProjectile, GunProjectileProperty.EXTRA_DATA.getTagName(), extraDataTag);
    }

    @Override
    default @Nullable _ExplosionData getExplosionData(Entity gunProjectile) {
        @Nullable CompoundTag extraDataTag = this.getExtraDataTag(gunProjectile);
        if (extraDataTag == null) return null;
        return NBTUtils.getStringJson(extraDataTag, GunProjectileProperty.EXPLOSION_DATA.getTagName(),
                _ExplosionData::fromJson);
    }
    @Override
    default void setExplosionData(Entity gunProjectile, _ExplosionData explosionData) {
        if (explosionData == null) return;
        @Nullable CompoundTag extraDataTag = this.getExtraStateTag(gunProjectile);
        if (extraDataTag == null) extraDataTag = new CompoundTag();
        NBTUtils.setStringJson(extraDataTag, GunProjectileProperty.EXPLOSION_DATA.getTagName(), explosionData,
                _ExplosionData::toJson);
        this.setExtraDataTag(gunProjectile, extraDataTag);
    }

    // --------IGunProjectileStateAccess--------

    @Override
    default @Nullable Vec3 getShootPos(Entity gunProjectile) {
        return NBTUtils.getVec3(gunProjectile, GunProjectileProperty.SHOOT_POS.getTagName());
    }
    @Override
    default void setShootPos(Entity gunProjectile, Vec3 shootPos) {
        NBTUtils.setVec3(gunProjectile, GunProjectileProperty.SHOOT_POS.getTagName(), shootPos);
    }

    @Override
    default @Nullable List<_DistanceDamageData> getDamageCalculation(Entity gunProjectile) {
        return NBTUtils.getStringJson(gunProjectile, GunProjectileProperty.DAMAGE_CALCULATION.getTagName(),
                (reader) -> JsonUtils.readList(reader, _DistanceDamageData::fromJson));
    }
    @Override
    default void setDamageCalculation(Entity gunProjectile, List<_DistanceDamageData> damageCalculation) {
        NBTUtils.setStringJson(gunProjectile, GunProjectileProperty.DAMAGE_CALCULATION.getTagName(), damageCalculation,
                (writer, value) -> JsonUtils.writeListValue(writer, value, _DistanceDamageData::toJson));
    }

    @Override
    default float getArmorIgnorePercent(Entity gunProjectile) {
        return NBTUtils.getFloat(gunProjectile, GunProjectileProperty.ARMOR_IGNORE_PERCENT.getTagName());
    }
    @Override
    default void setArmorIgnorePercent(Entity gunProjectile, float armorIgnorePercent) {
        NBTUtils.setFloat(gunProjectile, GunProjectileProperty.ARMOR_IGNORE_PERCENT.getTagName(), armorIgnorePercent);
    }

    @Override
    default float getHeadshotMultiplier(Entity gunProjectile) {
        return NBTUtils.getFloat(gunProjectile, GunProjectileProperty.HEADSHOT_MULTIPLIER.getTagName());
    }
    @Override
    default void setHeadshotMultiplier(Entity gunProjectile, float headshotMultiplier) {
        NBTUtils.setFloat(gunProjectile, GunProjectileProperty.HEADSHOT_MULTIPLIER.getTagName(), headshotMultiplier);
    }

    @Override
    default int getLifetimeTicks(Entity gunProjectile) {
        return NBTUtils.getInt(gunProjectile, GunProjectileProperty.LIFETIME_TICKS.getTagName());
    }
    @Override
    default void setLifetimeTicks(Entity gunProjectile, int lifetimeTicks) {
        NBTUtils.setInt(gunProjectile, GunProjectileProperty.LIFETIME_TICKS.getTagName(), lifetimeTicks);
    }

    @Override
    default float getBulletSpeed(Entity gunProjectile) {
        return NBTUtils.getFloat(gunProjectile, GunProjectileProperty.BULLET_SPEED.getTagName());
    }
    @Override
    default void setBulletSpeed(Entity gunProjectile, float bulletSpeed) {
        NBTUtils.setFloat(gunProjectile, GunProjectileProperty.BULLET_SPEED.getTagName(), bulletSpeed);
    }

    @Override
    default float getGravity(Entity gunProjectile) {
        return NBTUtils.getFloat(gunProjectile, GunProjectileProperty.GRAVITY.getTagName());
    }
    @Override
    default void setGravity(Entity gunProjectile, float gravity) {
        NBTUtils.setFloat(gunProjectile, GunProjectileProperty.GRAVITY.getTagName(), gravity);
    }

    @Override
    default float getFriction(Entity gunProjectile) {
        return NBTUtils.getFloat(gunProjectile, GunProjectileProperty.FRICTION.getTagName());
    }
    @Override
    default void setFriction(Entity gunProjectile, float friction) {
        NBTUtils.setFloat(gunProjectile, GunProjectileProperty.FRICTION.getTagName(), friction);
    }

    @Override
    default int getPierce(Entity gunProjectile) {
        return NBTUtils.getInt(gunProjectile, GunProjectileProperty.PIERCE.getTagName());
    }
    @Override
    default void setPierce(Entity gunProjectile, int pierce) {
        NBTUtils.setInt(gunProjectile, GunProjectileProperty.PIERCE.getTagName(), pierce);
    }

    @Override
    default boolean getIsTracer(Entity gunProjectile) {
        return NBTUtils.getBoolean(gunProjectile, GunProjectileProperty.IS_TRACER.getTagName());
    }
    @Override
    default void setIsTracer(Entity gunProjectile, boolean isTracer) {
        NBTUtils.setBoolean(gunProjectile, GunProjectileProperty.IS_TRACER.getTagName(), isTracer);
    }

    @Override
    default boolean getFireAspect(Entity gunProjectile) {
        return NBTUtils.getBoolean(gunProjectile, GunProjectileProperty.FIRE_ASPECT.getTagName());
    }
    @Override
    default void setFireAspect(Entity gunProjectile, boolean fireAspect) {
        NBTUtils.setBoolean(gunProjectile, GunProjectileProperty.FIRE_ASPECT.getTagName(), fireAspect);
    }

    @Override
    default int getFireAspectSeconds(Entity gunProjectile) {
        return NBTUtils.getInt(gunProjectile, GunProjectileProperty.FIRE_ASPECT_SECONDS.getTagName());
    }
    @Override
    default void setFireAspectSeconds(Entity gunProjectile, int fireAspectSeconds) {
        NBTUtils.setInt(gunProjectile, GunProjectileProperty.FIRE_ASPECT_SECONDS.getTagName(), fireAspectSeconds);
    }

    @Override
    default float getKnockbackStrength(Entity gunProjectile) {
        return NBTUtils.getFloat(gunProjectile, GunProjectileProperty.KNOCKBACK_STRENGTH.getTagName());
    }
    @Override
    default void setKnockbackStrength(Entity gunProjectile, float knockbackStrength) {
        NBTUtils.setFloat(gunProjectile, GunProjectileProperty.KNOCKBACK_STRENGTH.getTagName(), knockbackStrength);
    }

    @Override
    default boolean hasExtraStateTag(Entity gunProjectile) {
        return NBTUtils.hasKey(gunProjectile, GunProjectileProperty.EXTRA_STATE.getTagName());
    }
    @Override
    default @Nullable CompoundTag getExtraStateTag(Entity gunProjectile) {
        return NBTUtils.getCompoundTag(gunProjectile, GunProjectileProperty.EXTRA_STATE.getTagName());
    }
    @Override
    default void setExtraStateTag(Entity gunProjectile, CompoundTag extraStateTag) {
        NBTUtils.setCompoundTag(gunProjectile, GunProjectileProperty.EXTRA_STATE.getTagName(), extraStateTag);
    }
}
