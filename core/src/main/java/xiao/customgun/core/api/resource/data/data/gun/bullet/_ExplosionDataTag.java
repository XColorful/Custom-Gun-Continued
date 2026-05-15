/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun.bullet;

public class _ExplosionDataTag {

    // 总开关
    public static final String ENABLE_EXPLODE = "explode";

    // 爆炸属性
    public static final String EXPLODE_DAMAGE = "damage";
    public static final String EXPLODE_SCALE = "radius";
    public static final String MAX_DELAY_SECONDS = "delay";

    // 爆炸规则
    public static final String ENABLE_KNOCKBACK = "knockback";
    public static final String ENABLE_WORLD_DESTRUCTION = "destroy_block";

    private _ExplosionDataTag() {}
}
