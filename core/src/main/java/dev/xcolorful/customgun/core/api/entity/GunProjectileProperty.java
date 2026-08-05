package dev.xcolorful.customgun.core.api.entity;

import dev.xcolorful.customgun.core.api.entity.projectile.IGunProjectileDataAccess;
import dev.xcolorful.customgun.core.api.entity.projectile.IGunProjectileStateAccess;
import dev.xcolorful.customgun.core.api.entity.projectile._IGunProjectilePropertyAccess;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum GunProjectileProperty implements ResourceTag {
    // IGunProjectileDataAccess
    MANAGER_GROUP(GunProjectilePropertyTag.MANAGER_GROUP,
            IGunProjectileDataAccess::getManagerGroupTag,
            IGunProjectileDataAccess::setManagerGroupTag),
    GUN_LOCATION(GunProjectilePropertyTag.GUN_LOCATION,
            IGunProjectileDataAccess::getGunLocation,
            IGunProjectileDataAccess::setGunLocation),
    GUN_DISPLAY_LOCATION(GunProjectilePropertyTag.GUN_DISPLAY_LOCATION,
            IGunProjectileDataAccess::getGunDisplayLocation,
            IGunProjectileDataAccess::setGunDisplayLocation),
    AMMO_LOCATION(GunProjectilePropertyTag.AMMO_LOCATION,
            IGunProjectileDataAccess::getAmmoLocation,
            IGunProjectileDataAccess::setAmmoLocation),
    EXTRA_DATA(GunProjectilePropertyTag.EXTRA_DATA,
            IGunProjectileDataAccess::getExtraDataTag,
            IGunProjectileDataAccess::setExtraDataTag),

    // IGunProjectileStateAccess
    SHOOT_POS(GunProjectilePropertyTag.SHOOT_POS,
            IGunProjectileDataAccess::getShootPos,
            IGunProjectileDataAccess::setShootPos),
    ARMOR_IGNORE_PERCENT(GunProjectilePropertyTag.ARMOR_IGNORE_PERCENT,
            IGunProjectileDataAccess::getArmorIgnorePercent,
            IGunProjectileDataAccess::setArmorIgnorePercent),
    HEADSHOT_MULTIPLIER(GunProjectilePropertyTag.HEADSHOT_MULTIPLIER,
            IGunProjectileDataAccess::getHeadshotMultiplier,
            IGunProjectileDataAccess::setHeadshotMultiplier),
    DAMAGE_CALCULATION(GunProjectilePropertyTag.DAMAGE_CALCULATION,
            IGunProjectileStateAccess::getDamageCalculation,
            IGunProjectileDataAccess::setDamageCalculation),
    LIFETIME_TICKS(GunProjectilePropertyTag.LIFETIME_TICKS,
            IGunProjectileDataAccess::getLifetimeTicks,
            IGunProjectileDataAccess::setLifetimeTicks),
    BULLET_SPEED(GunProjectilePropertyTag.BULLET_SPEED,
            IGunProjectileDataAccess::getBulletSpeed,
            IGunProjectileDataAccess::setBulletSpeed),
    GRAVITY(GunProjectilePropertyTag.GRAVITY,
            IGunProjectileDataAccess::getGravity,
            IGunProjectileDataAccess::setGravity),
    FRICTION(GunProjectilePropertyTag.FRICTION,
            IGunProjectileDataAccess::getFriction,
            IGunProjectileDataAccess::setFriction),
    PIERCE(GunProjectilePropertyTag.PIERCE,
            IGunProjectileDataAccess::getPierce,
            IGunProjectileDataAccess::setPierce),
    IS_TRACER(GunProjectilePropertyTag.IS_TRACER,
            IGunProjectileDataAccess::getIsTracer,
            IGunProjectileDataAccess::setIsTracer),
    FIRE_ASPECT(GunProjectilePropertyTag.FIRE_ASPECT,
            IGunProjectileDataAccess::getFireAspect,
            IGunProjectileDataAccess::setFireAspect),
    FIRE_ASPECT_SECONDS(GunProjectilePropertyTag.FIRE_ASPECT_SECONDS,
            IGunProjectileDataAccess::getFireAspectSeconds,
            IGunProjectileDataAccess::setFireAspectSeconds),
    KNOCKBACK_STRENGTH(GunProjectilePropertyTag.KNOCKBACK_STRENGTH,
            IGunProjectileDataAccess::getKnockbackStrength,
            IGunProjectileDataAccess::setKnockbackStrength),
    EXTRA_STATE(GunProjectilePropertyTag.EXTRA_STATE,
            IGunProjectileDataAccess::getExtraStateTag,
            IGunProjectileDataAccess::setExtraStateTag),
    EXPLOSION_DATA(GunProjectilePropertyTag.EXPLOSION_DATA,
            IGunProjectileDataAccess::getExplosionData,
            IGunProjectileDataAccess::setExplosionData);

    public final String propertyName;
    private final BiFunction<IGunProjectileDataAccess, Entity, ?> getter;
    private final TriConsumer<IGunProjectileDataAccess, Entity, ?> setter;
    <T> GunProjectileProperty(String name, BiFunction<IGunProjectileDataAccess, Entity, T> getter, TriConsumer<IGunProjectileDataAccess, Entity, T> setter) {
        this.propertyName = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, GunProjectileProperty> PROPERTY_TYPE = new HashMap<>();

    static {
        for (GunProjectileProperty property : values()) {
            PROPERTY_TYPE.put(property.propertyName, property);
        }
    }

    public static @Nullable GunProjectileProperty fromString(String name) {
        return name != null ? PROPERTY_TYPE.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }

    /**
     * {@link _IGunProjectilePropertyAccess}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(IGunProjectileDataAccess access, Entity entity) {
        if (this.getter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support read operations.");
        }
        return (T) this.getter.apply(access, entity);
    }
    /**
     * 设置枪射物属性
     * <p>
     * <b>注意：</b>若通过脚本引擎间接调用此方法，请务必保证传递的 {@code value} 类型与该属性期望的 Java 类型完全一致
     * <p>
     * 例如整型属性必须传入 {@link java.lang.Integer}，否则会导致底层 Setter 强转崩溃
     */
    @SuppressWarnings("unchecked")
    public <T> void set(IGunProjectileDataAccess access, Entity entity, T value) {
        if (this.setter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support write operations.");
        }
        ((TriConsumer<IGunProjectileDataAccess, Entity, T>) this.setter).accept(access, entity, value);
    }
}
