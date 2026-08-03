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
    SHOOT_SOUND(GunSoundTypeTag.SHOOT_SOUND,null, true),
    SHOOT_3P_SOUND(GunSoundTypeTag.SHOOT_3P_SOUND,null, true),
    SILENCE_SOUND(GunSoundTypeTag.SILENCE_SOUND,null, true),
    SILENCE_3P_SOUND(GunSoundTypeTag.SILENCE_3P_SOUND,null, true),
    // 近战
    MELEE_BAYONET(GunSoundTypeTag.MELEE_BAYONET,null, false),
    MELEE_PUSH(GunSoundTypeTag.MELEE_PUSH,null, false),
    MELEE_STOCK(GunSoundTypeTag.MELEE_STOCK,null, false),
    // 动作
    DRY_FIRE_SOUND(GunSoundTypeTag.DRY_FIRE_SOUND,null, true),
    RELOAD_EMPTY_SOUND(GunSoundTypeTag.RELOAD_EMPTY_SOUND,null, true),
    RELOAD_TACTICAL_SOUND(GunSoundTypeTag.RELOAD_TACTICAL_SOUND,null, true),
    /**
     * EMPTY指的是枪管没有子弹，而不是弹匣没有子弹
     */
    INSPECT_EMPTY_SOUND(GunSoundTypeTag.INSPECT_EMPTY_SOUND,null, true),
    INSPECT_SOUND(GunSoundTypeTag.INSPECT_SOUND,null, true),
    DRAW_SOUND(GunSoundTypeTag.DRAW_SOUND,null, true),
    PUT_AWAY_SOUND(GunSoundTypeTag.PUT_AWAY_SOUND,null, true),
    BOLT_SOUND(GunSoundTypeTag.BOLT_SOUND,null, true),
    SWITCH_FIRE_MODE(GunSoundTypeTag.SWITCH_FIRE_MODE,GunSoundTypeTag.SWITCH_FIRE_MODE_OLD1, true),
    // 反馈
    HEAD_HIT_SOUND(GunSoundTypeTag.HEAD_HIT_SOUND,null, false),
    FLESH_HIT_SOUND(GunSoundTypeTag.FLESH_HIT_SOUND,null, false),
    KILL_SOUND(GunSoundTypeTag.KILL_SOUND,null, false);

    public final String typeName;
    public final String typeNameOld;
    public final boolean preload;
    GunSoundType(String name, String nameOld, boolean preload) {
        this.typeName = name;
        this.typeNameOld = nameOld;
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
            if (type.typeNameOld != null) SOUND_TYPES.put(type.typeNameOld, type);
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