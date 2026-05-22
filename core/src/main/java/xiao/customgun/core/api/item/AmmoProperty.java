/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.ammo.IAmmoDataAccess; // 请根据你项目的实际包路径调整
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum AmmoProperty implements ResourceTag {
    // IAmmoDataAccess
    AMMO_LOCATION(AmmoPropertyTag.AMMO_LOCATION,
            IAmmoDataAccess::getAmmoLocation,
            IAmmoDataAccess::setAmmoLocation);

    public final String propertyName;
    private final BiFunction<IAmmoDataAccess, ItemStack, ?> getter;
    private final TriConsumer<IAmmoDataAccess, ItemStack, ?> setter;

    <T> AmmoProperty(final String name, @Nullable BiFunction<IAmmoDataAccess, ItemStack, T> getter, @Nullable TriConsumer<IAmmoDataAccess, ItemStack, T> setter) {
        this.propertyName = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, AmmoProperty> PROPERTY_TYPE = new HashMap<>();

    static {
        for (AmmoProperty property : AmmoProperty.values()) {
            PROPERTY_TYPE.put(property.propertyName, property);
        }
    }

    public static @Nullable AmmoProperty fromString(String name) {
        return name != null ? PROPERTY_TYPE.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(IAmmoDataAccess access, ItemStack stack) {
        if (this.getter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support read operations.");
        }
        return (T) this.getter.apply(access, stack);
    }
    /**
     * 设置子弹属性
     * <p>
     * <b>注意：</b>若通过脚本引擎间接调用此方法，请务必保证传递的 {@code value} 类型与该属性期望的 Java 类型完全一致
     */
    @SuppressWarnings("unchecked")
    public <T> void set(IAmmoDataAccess access, ItemStack stack, T value) {
        if (this.setter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support write operations.");
        }
        ((TriConsumer<IAmmoDataAccess, ItemStack, T>) this.setter).accept(access, stack, value);
    }
}