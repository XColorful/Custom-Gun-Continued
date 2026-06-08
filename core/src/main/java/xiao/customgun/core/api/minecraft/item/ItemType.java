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
    // Item
    GUN(ItemTypeTag.GUN, ItemTypeTag.GUN_OLD1),
    ATTACHMENT(ItemTypeTag.ATTACHMENT, null),
    AMMO(ItemTypeTag.AMMO, null),
    AMMO_BOX(ItemTypeTag.AMMO_BOX, null),

    /*
    放在扩展模组里更好
     */
    // BlockItem
    @Deprecated WORKBENCH_212(ItemTypeTag.WORKBENCH_212, ItemTypeTag.WORKBENCH_212_OLD1),
    @Deprecated WORKBENCH_111(ItemTypeTag.WORKBENCH_111, ItemTypeTag.WORKBENCH_111_OLD1),
    @Deprecated WORKBENCH_211(ItemTypeTag.WORKBENCH_211, ItemTypeTag.WORKBENCH_211_OLD1),
    @Deprecated WORKBENCH_121(ItemTypeTag.WORKBENCH_121, ItemTypeTag.WORKBENCH_121_OLD1),
    @Deprecated STATUE(ItemTypeTag.STATUE, null),
    @Deprecated TARGET(ItemTypeTag.TARGET, null),
    // Item (Entity)
    @Deprecated TARGET_MINECART(ItemTypeTag.TARGET_MINECART, null),;

    public final String typeName;
    public final String typeNameOld;
    public final String registryName;
    ItemType(String name, String nameOld) {
        this.typeName = name;
        this.typeNameOld = nameOld;
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
            if (type.typeNameOld != null) ITEM_TYPES.put(type.typeNameOld, type);
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