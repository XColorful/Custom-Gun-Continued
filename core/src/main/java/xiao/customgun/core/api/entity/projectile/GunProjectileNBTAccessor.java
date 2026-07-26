package xiao.customgun.core.api.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.GunProjectileProperty;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import xiao.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import xiao.customgun.core.util.JsonUtils;
import xiao.customgun.core.util.NBTUtils;

import java.util.List;

public interface GunProjectileNBTAccessor extends GunProjectileValueAccessor, IGunProjectileNBTAccess {

    GunProjectileNBTAccessor INSTANCE = new GunProjectileNBTAccessor() {};

    // --------IGunProjectileNBTAccess--------

    @Override
    default @Nullable String getManagerGroupTag(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getString(gunProjectileCustomDataTag, GunProjectileProperty.MANAGER_GROUP.getTagName());
    }
    @Override
    default void setManagerGroupTag(CompoundTag gunProjectileCustomDataTag, String managerGroupTag) {
        NBTUtils.setString(gunProjectileCustomDataTag, GunProjectileProperty.MANAGER_GROUP.getTagName(), managerGroupTag);
    }

    @Override
    default @NotNull Identifier getGunLocation(CompoundTag gunProjectileCustomDataTag) {
        var gunLocation = NBTUtils.getResourceLocation(gunProjectileCustomDataTag, GunProjectileProperty.GUN_LOCATION.getTagName());
        return gunLocation != null ? gunLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunLocation(CompoundTag gunProjectileCustomDataTag, Identifier gunLocation) {
        NBTUtils.setResourceLocation(gunProjectileCustomDataTag, GunProjectileProperty.GUN_LOCATION.getTagName(), gunLocation);
    }

    @Override
    default @NotNull Identifier getGunDisplayLocation(CompoundTag gunProjectileCustomDataTag) {
        var gunDisplayLocation = NBTUtils.getResourceLocation(gunProjectileCustomDataTag, GunProjectileProperty.GUN_DISPLAY_LOCATION.getTagName());
        return gunDisplayLocation != null ? gunDisplayLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunDisplayLocation(CompoundTag gunProjectileCustomDataTag, Identifier gunDisplayLocation) {
        NBTUtils.setResourceLocation(gunProjectileCustomDataTag, GunProjectileProperty.GUN_DISPLAY_LOCATION.getTagName(), gunDisplayLocation);
    }

    @Override
    default @NotNull Identifier getAmmoLocation(CompoundTag gunProjectileCustomDataTag) {
        var ammoLocation = NBTUtils.getResourceLocation(gunProjectileCustomDataTag, GunProjectileProperty.AMMO_LOCATION.getTagName());
        return ammoLocation != null ? ammoLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAmmoLocation(CompoundTag gunProjectileCustomDataTag, Identifier ammoLocation) {
        NBTUtils.setResourceLocation(gunProjectileCustomDataTag, GunProjectileProperty.AMMO_LOCATION.getTagName(), ammoLocation);
    }

    @Override
    default boolean hasExtraDataTag(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.hasKey(gunProjectileCustomDataTag, GunProjectileProperty.EXTRA_DATA.getTagName());
    }
    @Override
    default @Nullable CompoundTag getExtraDataTag(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getCompoundTag(gunProjectileCustomDataTag, GunProjectileProperty.EXTRA_DATA.getTagName());
    }
    @Override
    default void setExtraDataTag(CompoundTag gunProjectileCustomDataTag, CompoundTag extraDataTag) {
        NBTUtils.setCompoundTag(gunProjectileCustomDataTag, GunProjectileProperty.EXTRA_DATA.getTagName(), extraDataTag);
    }

    // --------IGunProjectileStateAccess--------

    @Override
    default @Nullable Vec3 getShootPos(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getVec3(gunProjectileCustomDataTag, GunProjectileProperty.SHOOT_POS.getTagName());
    }
    @Override
    default void setShootPos(CompoundTag gunProjectileCustomDataTag, Vec3 shootPos) {
        NBTUtils.setVec3(gunProjectileCustomDataTag, GunProjectileProperty.SHOOT_POS.getTagName(), shootPos);
    }

    @Override
    default @Nullable List<_DistanceDamageData> getDamageCalculation(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getStringJson(gunProjectileCustomDataTag, GunProjectileProperty.DAMAGE_CALCULATION.getTagName(),
                (reader) -> JsonUtils.readList(reader, _DistanceDamageData::fromJson));
    }
    @Override
    default void setDamageCalculation(CompoundTag gunProjectileCustomDataTag, List<_DistanceDamageData> damageCalculation) {
        NBTUtils.setStringJson(gunProjectileCustomDataTag, GunProjectileProperty.DAMAGE_CALCULATION.getTagName(), damageCalculation,
                (writer, value) -> JsonUtils.writeListValue(writer, value, _DistanceDamageData::toJson));
    }

    @Override
    default float getArmorIgnorePercent(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getFloat(gunProjectileCustomDataTag, GunProjectileProperty.ARMOR_IGNORE_PERCENT.getTagName());
    }
    @Override
    default void setArmorIgnorePercent(CompoundTag gunProjectileCustomDataTag, float armorIgnorePercent) {
        NBTUtils.setFloat(gunProjectileCustomDataTag, GunProjectileProperty.ARMOR_IGNORE_PERCENT.getTagName(), armorIgnorePercent);
    }

    @Override
    default float getHeadshotMultiplier(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getFloat(gunProjectileCustomDataTag, GunProjectileProperty.HEADSHOT_MULTIPLIER.getTagName());
    }
    @Override
    default void setHeadshotMultiplier(CompoundTag gunProjectileCustomDataTag, float headshotMultiplier) {
        NBTUtils.setFloat(gunProjectileCustomDataTag, GunProjectileProperty.HEADSHOT_MULTIPLIER.getTagName(), headshotMultiplier);
    }

    @Override
    default int getLifetimeTicks(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getInt(gunProjectileCustomDataTag, GunProjectileProperty.LIFETIME_TICKS.getTagName());
    }
    @Override
    default void setLifetimeTicks(CompoundTag gunProjectileCustomDataTag, int lifetimeTicks) {
        NBTUtils.setInt(gunProjectileCustomDataTag, GunProjectileProperty.LIFETIME_TICKS.getTagName(), lifetimeTicks);
    }

    @Override
    default float getBulletSpeed(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getFloat(gunProjectileCustomDataTag, GunProjectileProperty.BULLET_SPEED.getTagName());
    }
    @Override
    default void setBulletSpeed(CompoundTag gunProjectileCustomDataTag, float bulletSpeed) {
        NBTUtils.setFloat(gunProjectileCustomDataTag, GunProjectileProperty.BULLET_SPEED.getTagName(), bulletSpeed);
    }

    @Override
    default float getGravity(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getFloat(gunProjectileCustomDataTag, GunProjectileProperty.GRAVITY.getTagName());
    }
    @Override
    default void setGravity(CompoundTag gunProjectileCustomDataTag, float gravity) {
        NBTUtils.setFloat(gunProjectileCustomDataTag, GunProjectileProperty.GRAVITY.getTagName(), gravity);
    }

    @Override
    default float getFriction(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getFloat(gunProjectileCustomDataTag, GunProjectileProperty.FRICTION.getTagName());
    }
    @Override
    default void setFriction(CompoundTag gunProjectileCustomDataTag, float friction) {
        NBTUtils.setFloat(gunProjectileCustomDataTag, GunProjectileProperty.FRICTION.getTagName(), friction);
    }

    @Override
    default int getPierce(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getInt(gunProjectileCustomDataTag, GunProjectileProperty.PIERCE.getTagName());
    }
    @Override
    default void setPierce(CompoundTag gunProjectileCustomDataTag, int pierce) {
        NBTUtils.setInt(gunProjectileCustomDataTag, GunProjectileProperty.PIERCE.getTagName(), pierce);
    }

    @Override
    default boolean getIsTracer(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getBoolean(gunProjectileCustomDataTag, GunProjectileProperty.IS_TRACER.getTagName());
    }
    @Override
    default void setIsTracer(CompoundTag gunProjectileCustomDataTag, boolean isTracer) {
        NBTUtils.setBoolean(gunProjectileCustomDataTag, GunProjectileProperty.IS_TRACER.getTagName(), isTracer);
    }

    @Override
    default boolean getFireAspect(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getBoolean(gunProjectileCustomDataTag, GunProjectileProperty.FIRE_ASPECT.getTagName());
    }
    @Override
    default void setFireAspect(CompoundTag gunProjectileCustomDataTag, boolean fireAspect) {
        NBTUtils.setBoolean(gunProjectileCustomDataTag, GunProjectileProperty.FIRE_ASPECT.getTagName(), fireAspect);
    }

    @Override
    default int getFireAspectSeconds(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getInt(gunProjectileCustomDataTag, GunProjectileProperty.FIRE_ASPECT_SECONDS.getTagName());
    }
    @Override
    default void setFireAspectSeconds(CompoundTag gunProjectileCustomDataTag, int fireAspectSeconds) {
        NBTUtils.setInt(gunProjectileCustomDataTag, GunProjectileProperty.FIRE_ASPECT_SECONDS.getTagName(), fireAspectSeconds);
    }

    @Override
    default float getKnockbackStrength(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getFloat(gunProjectileCustomDataTag, GunProjectileProperty.KNOCKBACK_STRENGTH.getTagName());
    }
    @Override
    default void setKnockbackStrength(CompoundTag gunProjectileCustomDataTag, float knockbackStrength) {
        NBTUtils.setFloat(gunProjectileCustomDataTag, GunProjectileProperty.KNOCKBACK_STRENGTH.getTagName(), knockbackStrength);
    }

    @Override
    default boolean hasExtraStateTag(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.hasKey(gunProjectileCustomDataTag, GunProjectileProperty.EXTRA_STATE.getTagName());
    }
    @Override
    default @Nullable CompoundTag getExtraStateTag(CompoundTag gunProjectileCustomDataTag) {
        return NBTUtils.getCompoundTag(gunProjectileCustomDataTag, GunProjectileProperty.EXTRA_STATE.getTagName());
    }
    @Override
    default void setExtraStateTag(CompoundTag gunProjectileCustomDataTag, CompoundTag extraStateTag) {
        NBTUtils.setCompoundTag(gunProjectileCustomDataTag, GunProjectileProperty.EXTRA_STATE.getTagName(), extraStateTag);
    }

    @Override
    default @Nullable _ExplosionData getExplosionData(CompoundTag gunProjectileCustomDataTag) {
        @Nullable CompoundTag extraDataTag = this.getExtraDataTag(gunProjectileCustomDataTag);
        if (extraDataTag == null) return null;
        return NBTUtils.getStringJson(extraDataTag, GunProjectileProperty.EXPLOSION_DATA.getTagName(),
                _ExplosionData::fromJson);
    }
    @Override
    default void setExplosionData(CompoundTag gunProjectileCustomDataTag, _ExplosionData explosionData) {
        if (explosionData == null) return;
        @Nullable CompoundTag extraDataTag = this.getExtraStateTag(gunProjectileCustomDataTag);
        if (extraDataTag == null) extraDataTag = new CompoundTag();
        NBTUtils.setStringJson(extraDataTag, GunProjectileProperty.EXPLOSION_DATA.getTagName(), explosionData,
                _ExplosionData::toJson);
        this.setExtraDataTag(gunProjectileCustomDataTag, extraDataTag);
    }
}
