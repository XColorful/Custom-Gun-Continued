/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.damage;

public class CustomDamageTypeTag {

    public static final String BULLET = "bullet"; public static final String BULLET_OLD1 = "bullet";
    public static final String PIERCER = "piercer"; public static final String PIERCER_OLD1 = "bullet_ignore_armor";
    public static final String BREAKER = "breaker"; public static final String BREAKER_OLD1 = "bullet_void";
    public static final String OVERRIDER = "overrider"; public static final String OVERRIDER_OLD1 = "bullet_void_ignore_armor";

    public static final String BULLET_DAMAGE = "bullet_damage";
    public static final String PIERCE_DAMAGE = "pierce_damage";
    public static final String BYPASS_DAMAGE = "bypass_damage";

    private CustomDamageTypeTag() {}
}
