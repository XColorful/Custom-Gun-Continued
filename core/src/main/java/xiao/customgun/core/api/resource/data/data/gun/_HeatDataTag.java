/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun;

import xiao.customgun.core.api.resource.data.data.GunDataTag;

public class _HeatDataTag {

    // 过热属性
    public static final String MAX_HEAT = "max_heat"; public static final String MAX_HEAT_OLD1 = "max";
    public static final String HEAT_PER_SHOT = "heat_per_shot"; public static final String HEAT_PER_SHOT_OLD1 = "per_shot";

    // 枪械属性
    /**
     * {@link GunDataTag#RPM}
     */
    public static final String MIN_RPM_BY_HEAT = "min_rpm_by_heat"; public static final String MIN_RPM_BY_HEAT_OLD1 = "min_rpm_mod";
    public static final String MAX_RPM_BY_HEAT = "max_rpm_by_heat"; public static final String MAX_RPM_BY_HEAT_OLD1 = "max_rpm_mod";
    /**
     * {@link GunDataTag#INACCURACY_DATA}
     */
    public static final String MIN_INACCURACY_BY_HEAT = "min_inaccuracy_by_heat"; public static final String MIN_INACCURACY_BY_HEAT_OLD1 = "min_inaccuracy";
    public static final String MAX_INACCURACY_BY_HEAT = "max_inaccuracy_by_heat"; public static final String MAX_INACCURACY_BY_HEAT_OLD1 = "max_inaccuracy";

    // 冷却属性
    public static final String OVERHEAT_LOCKTIME_MS = "overheat_locktime_ms"; public static final String OVERHEAT_LOCKTIME_MS_OLD1 = "over_heat_time";
    public static final String COOLING_DELAY_MS = "cooling_delay_ms"; public static final String COOLING_DELAY_MS_OLD1 = "cooling_delay";
    public static final String COOLING_SPEED_MULTIPLIER = "cooling_speed_multiplier"; public static final String COOLING_SPEED_MULTIPLIER_OLD1 = "cooling_multiplier";

    private _HeatDataTag() {}
}
