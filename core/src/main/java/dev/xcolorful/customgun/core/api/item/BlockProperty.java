/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.block.IBlockDataAccess;
import dev.xcolorful.customgun.core.api.item.block._IBlockPropertyAccess;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum BlockProperty implements ResourceTag {
    // IBlockDataAccess
    BLOCK_LOCATION(BlockPropertyTag.BLOCK_LOCATION,
            IBlockDataAccess::getBlockLocation,
            IBlockDataAccess::setBlockLocation);

    public final String propertyName;
    private final BiFunction<IBlockDataAccess, ItemStack, ?> getter;
    private final TriConsumer<IBlockDataAccess, ItemStack, ?> setter;
    <T> BlockProperty(final String name, @Nullable BiFunction<IBlockDataAccess, ItemStack, T> getter, @Nullable TriConsumer<IBlockDataAccess, ItemStack, T> setter) {
        this.propertyName = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, BlockProperty> PROPERTY_TYPE = new HashMap<>();

    static {
        for (BlockProperty property : BlockProperty.values()) {
            PROPERTY_TYPE.put(property.propertyName, property);
        }
    }

    public static @Nullable BlockProperty fromString(String name) {
        return name != null ? PROPERTY_TYPE.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }

    /**
     * {@link _IBlockPropertyAccess}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(IBlockDataAccess access, ItemStack stack) {
        if (this.getter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support read operations.");
        }
        return (T) this.getter.apply(access, stack);
    }
    /**
     * 设置方块属性
     * <p>
     * <b>注意：</b>若通过脚本引擎间接调用此方法，请务必保证传递的 {@code value} 类型与该属性期望的 Java 类型完全一致
     */
    @SuppressWarnings("unchecked")
    public <T> void set(IBlockDataAccess access, ItemStack stack, T value) {
        if (this.setter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support write operations.");
        }
        ((TriConsumer<IBlockDataAccess, ItemStack, T>) this.setter).accept(access, stack, value);
    }
}
