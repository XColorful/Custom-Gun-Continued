/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun;

import xiao.customgun.core.api.item.gun.ChargeType;

public class _ChargingDataTag {

    /**
     * {@link ChargeType}
     */
    public static final String CHARGE_TYPE = "type";
    public static final String MAX_CHARGE = "max_charge";
    public static final String FIRE_THRESHOLD = "fire_threshold";
    public static final String RECOVER_BY_FIRE = "decrease_on_fire";

    // 时间属性
    public static final String CHARGE_PER_TICK = "increase_per_tick";
    public static final String RECOVER_PER_TICK = "decrease_per_tick";
    public static final String ENABLE_CHARGE_DURING_COOLDOWN = "charge_during_cooldown";

    private _ChargingDataTag() {}
}
