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
    public static final String CHARGE_TYPE = "charge_type"; public static final String CHARGE_TYPE_OLD1 = "type";
    public static final String MAX_CHARGE = "max_charge";
    public static final String FIRE_THRESHOLD = "fire_threshold";
    public static final String RECOVER_BY_FIRE = "recover_by_fire"; public static final String RECOVER_BY_FIRE_OLD1 = "decrease_on_fire";

    // 时间属性
    public static final String CHARGE_PER_TICK = "charge_per_tick"; public static final String CHARGE_PER_TICK_OLD1 = "increase_per_tick";
    public static final String RECOVER_PER_TICK = "recover_per_tick"; public static final String RECOVER_PER_TICK_OLD1 = "decrease_per_tick";
    public static final String ENABLE_CHARGE_DURING_COOLDOWN = "enable_charge_during_cooldown"; public static final String ENABLE_CHARGE_DURING_COOLDOWN_OLD1 = "charge_during_cooldown";

    private _ChargingDataTag() {}
}
