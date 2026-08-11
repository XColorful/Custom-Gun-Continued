/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.item.gun;

import dev.xcolorful.customgun.core.api.item.gun.ThirdPersonAnimationTypeTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum ShooterAnimationCategory implements IShooterAnimationCategory {
    DEFAULT(ThirdPersonAnimationTypeTag.DEFAULT),
    MINIGUN(ThirdPersonAnimationTypeTag.MINIGUN);

    public final String typeName;
    ShooterAnimationCategory(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, IShooterAnimationCategory> ANIMATION_TYPES = new HashMap<>();
    @ApiStatus.Internal
    public static void registerAnimationCategory(IShooterAnimationCategory category) {
        ANIMATION_TYPES.put(category.getName(), category);
    }

    static {
        for (ShooterAnimationCategory type : values()) {
            registerAnimationCategory(type);
        }
    }

    public static @Nullable IShooterAnimationCategory fromString(String name) {
        return name != null ? ANIMATION_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}