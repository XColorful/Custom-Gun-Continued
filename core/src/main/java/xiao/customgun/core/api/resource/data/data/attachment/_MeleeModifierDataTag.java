/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.attachment;

import xiao.customgun.core.api.resource.data.data.gun._BulletDataTag;

public class _MeleeModifierDataTag {

    // 近战属性
    public static final String MELEE_DAMAGE = "melee_damage"; public static final String MELEE_DAMAGE_OLD1 = "damage";
    public static final String MELEE_DISTANCE = "melee_distance"; public static final String MELEE_DISTANCE_OLD1 = "distance";
    public static final String RANGE_ANGLE = "range_angle";

    // 时间属性
    public static final String DAMAGE_DELAY_SECONDS = "damage_delay_seconds"; public static final String DAMAGE_DELAY_SECONDS_OLD1 = "prep";
    public static final String BASE_COOLDOWN = "base_cooldown"; public static final String BASE_COOLDOWN_OLD1 = "cooldown";

    // 命中效果
    public static final String KNOCKBACK_STRENGTH = _BulletDataTag.KNOCKBACK_STRENGTH; public static final String KNOCKBACK_STRENGTH_OLD1 = "knockback";
    public static final String TARGET_EFFECT = "target_effect"; public static final String TARGET_EFFECT_OLD1 = "effects";

    private _MeleeModifierDataTag() {}
}
