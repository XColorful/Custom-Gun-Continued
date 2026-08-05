/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.item.gun;

import dev.xcolorful.customgun.core.api.item.gun.ThirdPersonAnimationTypeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum ThirdPersonAnimationType implements ResourceTag.CategoryTag {
    DEFAULT(ThirdPersonAnimationTypeTag.DEFAULT),
    MINIGUN(ThirdPersonAnimationTypeTag.MINIGUN);

    public final String typeName;
    ThirdPersonAnimationType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, ThirdPersonAnimationType> ANIMATION_TYPES = new HashMap<>();

    static {
        for (ThirdPersonAnimationType type : values()) {
            ANIMATION_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable ThirdPersonAnimationType fromString(String name) {
        return name != null ? ANIMATION_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}