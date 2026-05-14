/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.item;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum ItemType implements ResourceTag.RegistryTag {
    GUN(ItemTypeTag.GUN),
    ATTACHMENT(ItemTypeTag.ATTACHMENT),
    AMMO(ItemTypeTag.AMMO);

    public final String typeName;
    public final String registryName;
    ItemType(String name) {
        this.typeName = name;
        this.registryName = String.format("%s:%s", CustomGun.MOD_ID, this.typeName);
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }

    private static final Map<String, ItemType> ITEM_TYPES = new HashMap<>();

    static {
        for (ItemType type : values()) {
            ITEM_TYPES.put(type.typeName, type);
            ITEM_TYPES.put(type.registryName, type);
        }
    }

    public static @Nullable ItemType fromString(String name) {
        return name != null ? ITEM_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}