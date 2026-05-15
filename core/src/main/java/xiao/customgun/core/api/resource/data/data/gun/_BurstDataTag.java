/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data.gun;

import xiao.customgun.core.api.resource.data.data.GunDataTag;
import xiao.customgun.core.api.resource.data.data.gun.bullet._BulletSkillDataTag;

public class _BurstDataTag {

    /**
     * {@link GunDataTag}
     */
    public static final String RPM = GunDataTag.RPM;

    /**
     * {@link _BulletDataTag}
     */
    public static final String DAMAGE = _BulletDataTag.DISPLAY_DAMAGE;
    // 子弹飞行参数
    public static final String BULLET_SPEED = _BulletDataTag.BULLET_SPEED;
    // 命中效果
    public static final String KNOCKBACK_STRENGTH = _BulletDataTag.KNOCKBACK_STRENGTH;

    /**
     * {@link _BulletSkillDataTag}
     */
    public static final String ARMOR_IGNORE_PERCENT = _BulletSkillDataTag.ARMOR_IGNORE_PERCENT;
    public static final String HEADSHOT_MULTIPLIER = _BulletSkillDataTag.HEADSHOT_MULTIPLIER;

    /**
     * {@link _InaccuracyDataTag}
     */
    public static final String AIM_INACCURACY = "aim_inaccuracy";
    public static final String OTHER_INACCURACY = "other_inaccuracy";
}
