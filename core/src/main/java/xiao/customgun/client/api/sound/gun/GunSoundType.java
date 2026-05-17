/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.sound.gun;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.sound.gun.GunSoundTypeTag;

import java.util.HashMap;
import java.util.Map;

public enum GunSoundType implements ResourceTag.CategoryTag {
    // 射击
    SHOOT_SOUND(GunSoundTypeTag.SHOOT_SOUND, true),
    SHOOT_3P_SOUND(GunSoundTypeTag.SHOOT_3P_SOUND, true),
    SILENCE_SOUND(GunSoundTypeTag.SILENCE_SOUND, true),
    SILENCE_3P_SOUND(GunSoundTypeTag.SILENCE_3P_SOUND, true),
    // 近战
    MELEE_BAYONET(GunSoundTypeTag.MELEE_BAYONET, false),
    MELEE_PUSH(GunSoundTypeTag.MELEE_PUSH, false),
    MELEE_STOCK(GunSoundTypeTag.MELEE_STOCK, false),
    // 动作
    DRY_FIRE_SOUND(GunSoundTypeTag.DRY_FIRE_SOUND, true),
    RELOAD_EMPTY_SOUND(GunSoundTypeTag.RELOAD_EMPTY_SOUND, true),
    RELOAD_TACTICAL_SOUND(GunSoundTypeTag.RELOAD_TACTICAL_SOUND, true),
    INSPECT_EMPTY_SOUND(GunSoundTypeTag.INSPECT_EMPTY_SOUND, true),
    INSPECT_SOUND(GunSoundTypeTag.INSPECT_SOUND, true),
    DRAW_SOUND(GunSoundTypeTag.DRAW_SOUND, true),
    PUT_AWAY_SOUND(GunSoundTypeTag.PUT_AWAY_SOUND, true),
    BOLT_SOUND(GunSoundTypeTag.BOLT_SOUND, true),
    FIRE_SELECT(GunSoundTypeTag.FIRE_SELECT, true),
    // 反馈
    HEAD_HIT_SOUND(GunSoundTypeTag.HEAD_HIT_SOUND, false),
    FLESH_HIT_SOUND(GunSoundTypeTag.FLESH_HIT_SOUND, false),
    KILL_SOUND(GunSoundTypeTag.KILL_SOUND, false);

    public final String typeName;
    public final boolean preload;
    GunSoundType(String name, boolean preload) {
        this.typeName = name;
        this.preload = preload;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, GunSoundType> SOUND_TYPES = new HashMap<>();

    static {
        for (GunSoundType type : values()) {
            SOUND_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable GunSoundType fromString(String name) {
        return name != null ? SOUND_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}