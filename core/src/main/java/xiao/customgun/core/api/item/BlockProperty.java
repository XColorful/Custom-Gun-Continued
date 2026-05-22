/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum BlockProperty implements ResourceTag {
    // IBlockDataAccess
    BLOCK_LOCATION(BlockPropertyTag.BLOCK_LOCATION);

    public final String propertyName;
    BlockProperty(String name) {
        this.propertyName = name;
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
}
