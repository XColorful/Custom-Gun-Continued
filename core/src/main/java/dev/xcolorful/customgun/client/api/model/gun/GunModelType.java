/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model.gun;

import dev.xcolorful.customgun.core.api.model.gun.GunModelTypeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum GunModelType implements ResourceTag.CategoryTag {
    DEFAULT(GunModelTypeTag.DEFAULT);

    public final String typeName;
    GunModelType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, GunModelType> MODEL_TYPES = new HashMap<>();

    static {
        for (GunModelType type : values()) {
            MODEL_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable GunModelType fromString(String name) {
        return name != null ? MODEL_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
