/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.ammobox.IAmmoBoxDataAccess;
import xiao.customgun.core.api.item.ammobox._IAmmoBoxPropertyAccess;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum AmmoBoxProperty implements ResourceTag {
    // IAmmoDataAccess
    AMMO_LOCATION(AmmoBoxPropertyTag.AMMO_LOCATION,
            IAmmoBoxDataAccess::getAmmoLocation,
            IAmmoBoxDataAccess::setAmmoLocation),
    INFINITE_FEED(AmmoBoxPropertyTag.INFINITE_FEED,
            IAmmoBoxDataAccess::hasInfiniteFeed,
            IAmmoBoxDataAccess::setInfiniteFeed),
    ALMIGHTY_AMMO(AmmoBoxPropertyTag.ALMIGHTY_AMMO,
            IAmmoBoxDataAccess::isAlmightyAmmo,
            IAmmoBoxDataAccess::setAlmightyAmmo),

    // IAmmoStateAccess
    TOOLTIP_MASK(AmmoBoxPropertyTag.TOOLTIP_MASK,
            IAmmoBoxDataAccess::getTooltipMask,
            IAmmoBoxDataAccess::setTooltipMask),

    // IAmmoExpAccess
    AMMO_LEVEL(AmmoBoxPropertyTag.AMMO_LEVEL,
            IAmmoBoxDataAccess::getAmmoLevel,
            IAmmoBoxDataAccess::setAmmoLevel),

    // IAmmoBoxDataAccess
    AMMO_COUNT(AmmoBoxPropertyTag.AMMO_COUNT,
            IAmmoBoxDataAccess::getAmmoCount,
            IAmmoBoxDataAccess::setAmmoCount),
    BOX_LEVEL(AmmoBoxPropertyTag.BOX_LEVEL,
            IAmmoBoxDataAccess::getBoxLevel,
            IAmmoBoxDataAccess::setBoxLevel),
    STATUS_MASK(AmmoBoxPropertyTag.STATUS_MASK,
            IAmmoBoxDataAccess::getStatusMask,
            null);

    public final String propertyName;
    private final BiFunction<IAmmoBoxDataAccess, ItemStack, ?> getter;
    private final TriConsumer<IAmmoBoxDataAccess, ItemStack, ?> setter;
    <T> AmmoBoxProperty(final String name, @Nullable BiFunction<IAmmoBoxDataAccess, ItemStack, T> getter, @Nullable TriConsumer<IAmmoBoxDataAccess, ItemStack, T> setter) {
        this.propertyName = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, AmmoBoxProperty> PROPERTY_TYPE = new HashMap<>();

    static {
        for (AmmoBoxProperty property : AmmoBoxProperty.values()) {
            PROPERTY_TYPE.put(property.propertyName, property);
        }
    }

    public static @Nullable AmmoBoxProperty fromString(String name) {
        return name != null ? PROPERTY_TYPE.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }

    /**
     * {@link _IAmmoBoxPropertyAccess}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(IAmmoBoxDataAccess access, ItemStack stack) {
        if (this.getter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support read operations.");
        }
        return (T) this.getter.apply(access, stack);
    }
    /**
     * 设置弹药盒属性
     * <p>
     * <b>注意：</b>若通过脚本引擎间接调用此方法，请务必保证传递的 {@code value} 类型与该属性期望的 Java 类型完全一致
     */
    @SuppressWarnings("unchecked")
    public <T> void set(IAmmoBoxDataAccess access, ItemStack stack, T value) {
        if (this.setter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support write operations.");
        }
        ((TriConsumer<IAmmoBoxDataAccess, ItemStack, T>) this.setter).accept(access, stack, value);
    }
}