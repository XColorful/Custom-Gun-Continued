/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun;

public class _BulletDataTag {

    // 显示数值
    public static final String DISPLAY_DAMAGE = "damage";
    public static final String BULLET_SKILL = "extra_damage";

    // 子弹飞行参数
    public static final String LIFETIME_SECONDS = "life";
    public static final String BULLET_SPEED = "speed";
    public static final String GRAVITY = "gravity";
    public static final String FRICTION = "friction";

    // 射击效果
    public static final String BULLET_SPILT_AMOUNT = "bullet_amount"; // 子弹分裂数 (霰弹枪)
    public static final String PIERCE_AMOUNT = "pierce"; // 穿透数
    public static final String TRACER_INTERVAL = "tracer_count_interval"; // 发射子弹为曳光弹的间隔

    // 命中效果
    public static final String FIRE_ASPECT = "ignite"; // 火焰附加
    public static final String FIRE_ASPECT_SECONDS = "ignite_entity_time";
    public static final String KNOCKBACK_STRENGTH = "knockback";
    public static final String BULLET_EXPLOSION = "explosion";
}
