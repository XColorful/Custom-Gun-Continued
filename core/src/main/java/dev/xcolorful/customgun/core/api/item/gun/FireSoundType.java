/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum FireSoundType implements ResourceTag.CategoryTag {
    NORMAL(FireSoundTypeTag.NORMAL),
    SILENCED(FireSoundTypeTag.SILENCED),
    MUTED(FireSoundTypeTag.MUTED);

    public final String typeName;
    FireSoundType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, FireSoundType> FIRE_SOUND_TYPES = new HashMap<>();

    static {
        for (FireSoundType type : values()) {
            FIRE_SOUND_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable FireSoundType fromString(String name) {
        return name != null ? FIRE_SOUND_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}