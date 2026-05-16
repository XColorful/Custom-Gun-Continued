/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun.bullet;

public class _ExplosionDataTag {

    // 总开关
    public static final String ENABLE_EXPLODE = "enable_explode"; public static final String ENABLE_EXPLODE_OLD1 = "explode";

    // 爆炸属性
    public static final String EXPLODE_DAMAGE = "explode_damage"; public static final String EXPLODE_DAMAGE_OLD1 = "damage";
    public static final String EXPLODE_SCALE = "explode_scale"; public static final String EXPLODE_SCALE_OLD1 = "radius";
    public static final String MAX_DELAY_SECONDS = "max_delay_seconds"; public static final String MAX_DELAY_SECONDS_OLD1 = "delay";

    // 爆炸规则
    public static final String ENABLE_KNOCKBACK = "enable_knockback"; public static final String ENABLE_KNOCKBACK_OLD1 = "knockback";
    public static final String ENABLE_WORLD_DESTRUCTION = "enable_world_destruction"; public static final String ENABLE_WORLD_DESTRUCTION_OLD1 = "destroy_block";

    private _ExplosionDataTag() {}
}
