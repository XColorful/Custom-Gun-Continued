/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun;

import xiao.customgun.core.api.resource.data.data.GunDataTag;
import xiao.customgun.core.api.resource.data.data.gun.bullet._BulletSkillDataTag;
import xiao.customgun.core.api.resource.data.data.gun.bullet.damage._DistanceDamageDataTag;

public class _FireModeDataTag {

    // 枪械属性
    public static final String RPM = GunDataTag.RPM;

    // 子弹属性
    /**
     * 理应同{@link _DistanceDamageDataTag}?
     */
    public static final String DAMAGE = "damage";
    public static final String BULLET_SPEED = _BulletDataTag.BULLET_SPEED;
    public static final String KNOCKBACK_STRENGTH = _BulletDataTag.KNOCKBACK_STRENGTH;

    // 子弹Skill
    public static final String ARMOR_IGNORE_PERCENT = _BulletSkillDataTag.ARMOR_IGNORE_PERCENT;
    public static final String HEADSHOT_MULTIPLIER = _BulletSkillDataTag.HEADSHOT_MULTIPLIER;

    // Burst模式
    public static final String AIM_INACCURACY = _BurstDataTag.AIM_INACCURACY;
    public static final String OTHER_INACCURACY = _BurstDataTag.OTHER_INACCURACY;

    private _FireModeDataTag() {}
}
