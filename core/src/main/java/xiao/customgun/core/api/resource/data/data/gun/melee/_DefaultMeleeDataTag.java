/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun.melee;

import xiao.customgun.core.api.resource.data.data.gun._BulletDataTag;

public class _DefaultMeleeDataTag {

    // 近战属性
    public static final String MELEE_DAMAGE = "damage";
    public static final String MELEE_DISTANCE = "distance";
    public static final String RANGE_ANGLE = "range_angle";

    // 时间属性
    public static final String DAMAGE_DELAY_SECONDS = "prep";
    public static final String BASE_COOLDOWN = "cooldown";

    // 命中效果
    public static final String KNOCKBACK_STRENGTH = _BulletDataTag.KNOCKBACK_STRENGTH;

    // 显示
    public static final String ANIMATION_TYPE = "animation_type";

    private _DefaultMeleeDataTag() {}
}
