/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun.melee;

import xiao.customgun.core.api.resource.data.data.attachment._MeleeModifierDataTag;

public class _DefaultMeleeDataTag {

    // 近战属性
    public static final String MELEE_DAMAGE = _MeleeModifierDataTag.MELEE_DAMAGE; public static final String MELEE_DAMAGE_OLD1 = "damage";
    public static final String MELEE_DISTANCE = _MeleeModifierDataTag.MELEE_DISTANCE; public static final String MELEE_DISTANCE_OLD1 = "distance";
    public static final String RANGE_ANGLE = _MeleeModifierDataTag.RANGE_ANGLE;

    // 时间属性
    public static final String DAMAGE_DELAY_SECONDS = _MeleeModifierDataTag.DAMAGE_DELAY_SECONDS; public static final String DAMAGE_DELAY_SECONDS_OLD1 = "prep";
    public static final String BASE_COOLDOWN = _MeleeModifierDataTag.BASE_COOLDOWN; public static final String BASE_COOLDOWN_OLD1 = "cooldown";

    // 命中效果
    public static final String KNOCKBACK_STRENGTH = _MeleeModifierDataTag.KNOCKBACK_STRENGTH; public static final String KNOCKBACK_STRENGTH_OLD1 = "knockback";

    // 显示
    public static final String ANIMATION_TYPE = "animation_type";

    private _DefaultMeleeDataTag() {}
}
