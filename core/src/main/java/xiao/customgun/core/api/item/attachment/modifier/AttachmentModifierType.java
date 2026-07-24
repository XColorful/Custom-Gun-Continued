/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment.modifier;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.item.attachment.modifier.AdsModifier;
import xiao.customgun.core.item.attachment.modifier.*;
import xiao.customgun.core.resource.data.data.attachment.*;
import xiao.customgun.core.resource.data.data.gun._InaccuracyData;

import java.util.HashMap;
import java.util.Map;

public enum AttachmentModifierType implements ResourceTag.CategoryTag {
    // 瞄准速度
    ADS(AttachmentModifierTypeTag.ADS,
            AdsModifier.INSTANCE),

    // 子弹属性
    DAMAGE_CALCULATION(AttachmentModifierTypeTag.DAMAGE_CALCULATION,
            DamageCalculationModifier.INSTANCE),
    HEADSHOT_MULTIPLIER(AttachmentModifierTypeTag.HEADSHOT_MULTIPLIER,
            HeadshotMultiplierModifier.INSTANCE),
    ARMOR_IGNORE_PERCENT(AttachmentModifierTypeTag.ARMOR_IGNORE_PERCENT,
            ArmorIgnoreModifier.INSTANCE),
    BULLET_SPEED(AttachmentModifierTypeTag.BULLET_SPEED,
            BulletSpeedModifier.INSTANCE),
    PIERCE_COUNT(AttachmentModifierTypeTag.PIERCE_COUNT,
            PierceCountModifier.INSTANCE),
    FIRE_ASPECT(AttachmentModifierTypeTag.FIRE_ASPECT,
            FireAspectModifier.INSTANCE),
    KNOCKBACK_STRENGTH(AttachmentModifierTypeTag.KNOCKBACK_STRENGTH,
            KnockbackStrengthModifier.INSTANCE),
    BULLET_EXPLOSION(AttachmentModifierTypeTag.BULLET_EXPLOSION,
            BulletExplosionModifier.INSTANCE),

    // 枪械属性
    RPM(AttachmentModifierTypeTag.RPM,
            RpmModifier.INSTANCE),
    RECOIL_DATA(AttachmentModifierTypeTag.RECOIL_DATA,
            RecoilDataModifier.INSTANCE),
    EFFECTIVE_RANGE(AttachmentModifierTypeTag.EFFECTIVE_RANGE,
            EffectiveRangeModifier.INSTANCE),
    WEIGHT(AttachmentModifierTypeTag.WEIGHT,
            WeightModifier.INSTANCE),
    MUZZLE(AttachmentModifierTypeTag.MUZZLE,
            MuzzleModifier.INSTANCE),
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    AIM_INACCURACY(AttachmentModifierTypeTag.AIM_INACCURACY,
            AimInaccuracyModifier.INSTANCE),
    SNEAK_INACCURACY(AttachmentModifierTypeTag.SNEAK_INACCURACY,
            SneakInaccuracyModifier.INSTANCE),
    PRONE_INACCURACY(AttachmentModifierTypeTag.PRONE_INACCURACY,
            ProneInaccuracyModifier.INSTANCE),
    OTHER_INACCURACY(AttachmentModifierTypeTag.OTHER_INACCURACY,
            OtherInaccuracyModifier.INSTANCE),
    // 近战
    MELEE(AttachmentModifierTypeTag.MELEE,
            MeleeModifier.INSTANCE),

    // 弹匣
    MAGAZINE_CATEGORY(AttachmentModifierTypeTag.MAGAZINE_CATEGORY,
            MagazineCategoryModifier.INSTANCE);

    public final String typeName;
    public final IAttachmentModifier<?, ?> modifier;
    <K, V> AttachmentModifierType(String name, IAttachmentModifier<K, V> modifier) {
        this.typeName = name;
        this.modifier = modifier;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    public IAttachmentModifier<?, ?> getModifier() {
        return this.modifier;
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