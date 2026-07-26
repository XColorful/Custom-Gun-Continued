/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun.modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.gun._InaccuracyData;

import java.util.HashMap;
import java.util.Map;

public enum GunModifierType implements ResourceTag.CategoryTag, ResourceTag.ConstantTag, IGunModifierType {
    // 瞄准速度
    ADS(GunModifierTypeTag.ADS),

    // 子弹属性
    DAMAGE_CALCULATION(GunModifierTypeTag.DAMAGE_CALCULATION),
    HEADSHOT_MULTIPLIER(GunModifierTypeTag.HEADSHOT_MULTIPLIER),
    ARMOR_IGNORE_PERCENT(GunModifierTypeTag.ARMOR_IGNORE_PERCENT),
    BULLET_SPEED(GunModifierTypeTag.BULLET_SPEED),
    PIERCE_COUNT(GunModifierTypeTag.PIERCE_COUNT),
    FIRE_ASPECT(GunModifierTypeTag.FIRE_ASPECT),
    KNOCKBACK_STRENGTH(GunModifierTypeTag.KNOCKBACK_STRENGTH),
    BULLET_EXPLOSION(GunModifierTypeTag.BULLET_EXPLOSION),

    // 枪械属性
    RPM(GunModifierTypeTag.RPM),
    RECOIL_DATA(GunModifierTypeTag.RECOIL_DATA),
    EFFECTIVE_RANGE(GunModifierTypeTag.EFFECTIVE_RANGE),
    WEIGHT(GunModifierTypeTag.WEIGHT),
    MUZZLE(GunModifierTypeTag.MUZZLE),
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    AIM_INACCURACY(GunModifierTypeTag.AIM_INACCURACY),
    SNEAK_INACCURACY(GunModifierTypeTag.SNEAK_INACCURACY),
    PRONE_INACCURACY(GunModifierTypeTag.PRONE_INACCURACY),
    OTHER_INACCURACY(GunModifierTypeTag.OTHER_INACCURACY),
    // 近战
    MELEE(GunModifierTypeTag.MELEE),

    // 弹匣
    MAGAZINE_CATEGORY(GunModifierTypeTag.MAGAZINE_CATEGORY);

    public final String typeName;
    GunModifierType(String name) {
        this.typeName = name;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }
    @Override public String getConstantName() {
        return this.typeName;
    }

    @Override
    public @NotNull GunModifierType getGunModifierType() {
        return this;
    }

    private static final Map<String, GunModifierType> MODIFIER_TYPES = new HashMap<>();

    static {
        for (GunModifierType type : values()) {
            MODIFIER_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable GunModifierType fromString(String name) {
        return name != null ? MODIFIER_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
