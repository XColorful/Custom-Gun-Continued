/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.gun._InaccuracyData;

import java.util.HashMap;
import java.util.Map;

public enum AttachmentModifierType implements ResourceTag.CategoryTag {
    // 瞄准速度
    ADS(AttachmentModifierTypeTag.ADS),

    // 子弹属性
    DAMAGE_CALCULATION(AttachmentModifierTypeTag.DAMAGE_CALCULATION),
    HEADSHOT_MULTIPLIER(AttachmentModifierTypeTag.HEADSHOT_MULTIPLIER),
    ARMOR_IGNORE_PERCENT(AttachmentModifierTypeTag.ARMOR_IGNORE_PERCENT),
    BULLET_SPEED(AttachmentModifierTypeTag.BULLET_SPEED),
    PIERCE_COUNT(AttachmentModifierTypeTag.PIERCE_COUNT),
    FIRE_ASPECT(AttachmentModifierTypeTag.FIRE_ASPECT),
    KNOCKBACK_STRENGTH(AttachmentModifierTypeTag.KNOCKBACK_STRENGTH),
    BULLET_EXPLOSION(AttachmentModifierTypeTag.BULLET_EXPLOSION),

    // 枪械属性
    RPM(AttachmentModifierTypeTag.RPM),
    RECOIL_DATA(AttachmentModifierTypeTag.RECOIL_DATA),
    EFFECTIVE_RANGE(AttachmentModifierTypeTag.EFFECTIVE_RANGE),
    WEIGHT(AttachmentModifierTypeTag.WEIGHT),
    MUZZLE(AttachmentModifierTypeTag.MUZZLE),
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    AIM_INACCURACY(AttachmentModifierTypeTag.AIM_INACCURACY),
    SNEAK_INACCURACY(AttachmentModifierTypeTag.SNEAK_INACCURACY),
    PRONE_INACCURACY(AttachmentModifierTypeTag.PRONE_INACCURACY),
    OTHER_INACCURACY(AttachmentModifierTypeTag.OTHER_INACCURACY),
    // 近战
    MELEE(AttachmentModifierTypeTag.MELEE),

    // 弹匣
    MAGAZINE_CATEGORY(AttachmentModifierTypeTag.MAGAZINE_CATEGORY);

    public final String typeName;
    AttachmentModifierType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, AttachmentModifierType> MODIFIER_TYPES = new HashMap<>();

    static {
        for (AttachmentModifierType type : values()) {
            MODIFIER_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable AttachmentModifierType fromString(String name) {
        return name != null ? MODIFIER_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}