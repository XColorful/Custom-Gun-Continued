/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.gun.IGunDataAccess;
import xiao.customgun.core.api.item.gun.IGunStateAccess;
import xiao.customgun.core.api.item.gun._IGunPropertyAccess;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum GunProperty implements ResourceTag {
    // IGunDataAccess
    GUN_LOCATION(GunPropertyTag.GUN_LOCATION,
            IGunDataAccess::getGunLocation,
            IGunDataAccess::setGunLocation),
    GUN_DISPLAY_LOCATION(GunPropertyTag.GUN_DISPLAY_LOCATION,
            IGunDataAccess::getGunDisplayLocation,
            IGunDataAccess::setGunDisplayLocation),

    // IGunStateAccess
    FIRE_MODE_TYPE(GunPropertyTag.FIRE_MODE_TYPE,
            IGunDataAccess::getFireModeType,
            IGunDataAccess::setFireModeType),
    HEAT(GunPropertyTag.HEAT,
            IGunDataAccess::getHeatCount,
            IGunDataAccess::setHeatCount),
    OVERHEAT_LOCK(GunPropertyTag.OVERHEAT_LOCK,
            IGunDataAccess::hasOverheatLock,
            IGunStateAccess::setOverheatLock),
    ATTACHMENT_LOCK(GunPropertyTag.ATTACHMENT_LOCK,
            IGunDataAccess::hasAttachmentLock,
            IGunDataAccess::setAttachmentLock),
    LASER_COLOR(GunPropertyTag.LASER_COLOR,
            IGunDataAccess::getLaserColorInt,
            IGunDataAccess::setLaserColorInt),
    // IGunAmmoDataAccess

    DUMMY_AMMO(GunPropertyTag.DUMMY_AMMO,
            IGunDataAccess::getDummyAmmoCount,
            IGunDataAccess::setDummyAmmoCount),
    DUMMY_AMMO_LIMIT(GunPropertyTag.DUMMY_AMMO_LIMIT,
            IGunDataAccess::getDummyAmmoLimit,
            IGunDataAccess::setDummyAmmoLimit),
    MAG_AMMO(GunPropertyTag.MAG_AMMO,
            IGunDataAccess::getMagAmmoCount,
            IGunDataAccess::setMagAmmoCount),
    BARREL_AMMO(GunPropertyTag.BARREL_AMMO,
            IGunDataAccess::getBarrelAmmoCount,
            IGunDataAccess::setBarrelAmmoCount),

    // IGunAttachmentDataAccess
    ATTACHMENT_PREFIX(GunPropertyTag.ATTACHMENT_PREFIX,
            null,
            null),

    // IGunExpAccess
    GUN_EXP(GunPropertyTag.GUN_EXP,
            IGunDataAccess::getGunExp,
            IGunDataAccess::setGunExp);

    public final String propertyName;
    private final BiFunction<IGunDataAccess, ItemStack, ?> getter;
    private final TriConsumer<IGunDataAccess, ItemStack, ?> setter;
    <T> GunProperty(String name, @Nullable BiFunction<IGunDataAccess, ItemStack, T> getter, @Nullable TriConsumer<IGunDataAccess, ItemStack, T> setter) {
        this.propertyName = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, GunProperty> PROPERTY_TYPE = new HashMap<>();

    static {
        for (GunProperty property : values()) {
            PROPERTY_TYPE.put(property.propertyName, property);
        }
    }

    public static @Nullable GunProperty fromString(String name) {
        return name != null ? PROPERTY_TYPE.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }

    /**
     * {@link _IGunPropertyAccess}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(IGunDataAccess access, ItemStack stack) {
        if (this.getter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support read operations.");
        }
        return (T) this.getter.apply(access, stack);
    }
    /**
     * 设置枪械属性
     * <p>
     * <b>注意：</b>若通过脚本引擎间接调用此方法，请务必保证传递的 {@code value} 类型与该属性期望的 Java 类型完全一致
     * <p>
     * 例如整型属性（如 DUMMY_AMMO）必须传入 {@link java.lang.Integer}，否则会导致底层 Setter 强转崩溃
     */
    @SuppressWarnings("unchecked")
    public <T> void set(IGunDataAccess access, ItemStack stack, T value) {
        if (this.setter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support write operations.");
        }
        ((TriConsumer<IGunDataAccess, ItemStack, T>) this.setter).accept(access, stack, value);
    }
}
