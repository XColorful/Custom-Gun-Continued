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
    public static final String MAX_HEAT = "max";
    public static final String HEAT_PER_SHOT = "per_shot";

    // 枪械属性
    /**
     * {@link GunDataTag#RPM}
     */
    public static final String MIN_RPM_BY_HEAT = "min_rpm_mod"; // 最小热量时的RPM
    public static final String MAX_RPM_BY_HEAT = "max_rpm_mod";
    /**
     * {@link GunDataTag#INACCURACY_DATA}
     */
    public static final String MIN_INACCURACY_BY_HEAT = "min_inaccuracy"; // 最小热量时的不准确度
    public static final String MAX_INACCURACY_BY_HEAT = "max_inaccuracy";

    // 冷却属性
    public static final String OVERHEAT_LOCKTIME_MS = "over_heat_time";
    public static final String COOLING_DELAY_MS = "cooling_delay";
    public static final String COOLING_SPEED_MULTIPLIER = "cooling_multiplier";

    private _HeatDataTag() {}
}
