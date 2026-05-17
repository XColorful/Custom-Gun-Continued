/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun;

public class _BulletDataTag {

    // 显示数值
    public static final String DISPLAY_DAMAGE = "display_damage"; public static final String DISPLAY_DAMAGE_OLD1 = "damage";
    public static final String BULLET_SKILL = "bullet_skill"; public static final String BULLET_SKILL_OLD1 = "extra_damage";

    // 子弹飞行参数
    public static final String LIFETIME_SECONDS = "lifetime_seconds"; public static final String LIFETIME_SECONDS_OLD1 = "life";
    public static final String BULLET_SPEED = "bullet_speed"; public static final String BULLET_SPEED_OLD1 = "speed";
    public static final String GRAVITY = "gravity";
    public static final String FRICTION = "friction";

    // 射击效果
    public static final String BULLET_SPILT_AMOUNT = "bullet_split_amount"; public static final String BULLET_SPILT_AMOUNT_OLD1 = "bullet_amount";
    public static final String PIERCE_COUNT = "pierce_count"; public static final String PIERCE_COUNT_OLD1 = "pierce";
    public static final String TRACER_INTERVAL = "tracer_interval"; public static final String TRACER_INTERVAL_OLD1 = "tracer_count_interval";

    // 命中效果
    public static final String FIRE_ASPECT = "fire_aspect"; public static final String FIRE_ASPECT_OLD1 = "ignite";
    public static final String FIRE_ASPECT_SECONDS = "fire_aspect_seconds"; public static final String FIRE_ASPECT_SECONDS_OLD1 = "ignite_entity_time";
    public static final String KNOCKBACK_STRENGTH = "knockback_strength"; public static final String KNOCKBACK_STRENGTH_OLD1 = "knockback";
    public static final String BULLET_EXPLOSION = "bullet_explosion"; public static final String BULLET_EXPLOSION_OLD1 = "explosion";

    private _BulletDataTag() {}
}
