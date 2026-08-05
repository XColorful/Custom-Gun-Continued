/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun.modifier;

import dev.xcolorful.customgun.core.api.resource.data.data.GunDataTag;
import dev.xcolorful.customgun.core.api.resource.data.data.gun._BulletDataTag;
import dev.xcolorful.customgun.core.api.resource.data.data.gun.bullet._BulletSkillDataTag;
import dev.xcolorful.customgun.core.resource.data.data.gun._InaccuracyData;

public class GunModifierTypeTag {

    // 瞄准速度
    public static final String ADS = "ads";

    // 子弹属性
    public static final String HEADSHOT_MULTIPLIER = _BulletSkillDataTag.HEADSHOT_MULTIPLIER;
    public static final String ARMOR_IGNORE_PERCENT = _BulletSkillDataTag.ARMOR_IGNORE_PERCENT;
    public static final String DAMAGE_CALCULATION = _BulletSkillDataTag.DAMAGE_CALCULATION;
    public static final String BULLET_SPEED = _BulletDataTag.BULLET_SPEED;
    public static final String PIERCE_COUNT = _BulletDataTag.PIERCE_COUNT;
    public static final String FIRE_ASPECT = _BulletDataTag.FIRE_ASPECT;
    public static final String KNOCKBACK_STRENGTH = _BulletDataTag.KNOCKBACK_STRENGTH;
    public static final String BULLET_EXPLOSION = _BulletDataTag.BULLET_EXPLOSION;

    // 枪械属性
    public static final String RPM = GunDataTag.RPM;
    public static final String RECOIL_DATA = GunDataTag.RECOIL_DATA;
    public static final String EFFECTIVE_RANGE = "effect_range";
    @Deprecated public static final String WEIGHT = "weight";
    public static final String MUZZLE = "muzzle";
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    public static final String AIM_INACCURACY = "aim_inaccuracy";
    public static final String SNEAK_INACCURACY = "sneak_inaccuracy";
    public static final String PRONE_INACCURACY = "prone_inaccuracy";
    public static final String OTHER_INACCURACY = "other_inaccuracy";
    // 近战
    public static final String MELEE = "melee";

    // 弹匣
    public static final String MAGAZINE_CATEGORY = "magazine_category";

    private GunModifierTypeTag() {}
}
