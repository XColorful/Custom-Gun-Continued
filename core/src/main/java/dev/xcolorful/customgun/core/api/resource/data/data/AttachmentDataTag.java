/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.data.data;

import dev.xcolorful.customgun.core.api.item.gun.modifier.GunModifierTypeTag;
import dev.xcolorful.customgun.core.resource.data.data.gun._InaccuracyData;

public class AttachmentDataTag {

    // 瞄准速度
    public static final String ADS = GunModifierTypeTag.ADS;

    // 子弹属性
    public static final String ARMOR_IGNORE_PERCENT = GunModifierTypeTag.ARMOR_IGNORE_PERCENT; public static final String ARMOR_IGNORE_PERCENT_OLD1 = "armor_ignore";
    public static final String HEADSHOT_MULTIPLIER = GunModifierTypeTag.HEADSHOT_MULTIPLIER; public static final String HEADSHOT_MULTIPLIER_OLD1 = "head_shot";
    public static final String DAMAGE_CALCULATION = GunModifierTypeTag.DAMAGE_CALCULATION; public static final String DAMAGE_CALCULATION_OLD1 = "damage";
    public static final String BULLET_SPEED = GunModifierTypeTag.BULLET_SPEED; public static final String BULLET_SPEED_OLD1 = "ammo_speed";
    public static final String PIERCE_COUNT = GunModifierTypeTag.PIERCE_COUNT; public static final String PIERCE_COUNT_OLD1 = "pierce";
    public static final String FIRE_ASPECT = GunModifierTypeTag.FIRE_ASPECT; public static final String FIRE_ASPECT_OLD1 = "ignite";
    public static final String KNOCKBACK_STRENGTH = GunModifierTypeTag.KNOCKBACK_STRENGTH; public static final String KNOCKBACK_STRENGTH_OLD1 = "knockback";
    public static final String BULLET_EXPLOSION = GunModifierTypeTag.BULLET_EXPLOSION; public static final String BULLET_EXPLOSION_OLD1 = "explosion";

    // 枪械属性
    public static final String RPM = GunModifierTypeTag.RPM;
    public static final String RECOIL_DATA = GunModifierTypeTag.RECOIL_DATA; public static final String RECOIL_DATA_OLD1 = "recoil";
    public static final String EFFECTIVE_RANGE = GunModifierTypeTag.EFFECTIVE_RANGE;
    @Deprecated public static final String WEIGHT = GunModifierTypeTag.WEIGHT; @Deprecated public static final String WEIGHT_OLD1 = "weight_modifier";
    public static final String MUZZLE = GunModifierTypeTag.MUZZLE; public static final String MUZZLE_OLD1 = "silence";
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    public static final String AIM_INACCURACY = GunModifierTypeTag.AIM_INACCURACY;
    public static final String SNEAK_INACCURACY = GunModifierTypeTag.SNEAK_INACCURACY;
    public static final String PRONE_INACCURACY = GunModifierTypeTag.PRONE_INACCURACY; public static final String PRONE_INACCURACY_OLD1 = "lie_inaccuracy";
    public static final String OTHER_INACCURACY = GunModifierTypeTag.OTHER_INACCURACY; public static final String OTHER_INACCURACY_OLD1 = "inaccuracy";
    // 近战
    public static final String MELEE = GunModifierTypeTag.MELEE;

    // 弹匣
    public static final String MAGAZINE_CATEGORY = GunModifierTypeTag.MAGAZINE_CATEGORY; public static final String MAGAZINE_CATEGORY_OLD1 = "extended_mag_level";

    private AttachmentDataTag() {}
}