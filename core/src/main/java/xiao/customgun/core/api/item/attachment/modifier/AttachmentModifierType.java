/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment.modifier;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.gun.modifier.GunModifierType;
import xiao.customgun.core.api.item.gun.modifier.IGunModifierType;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.item.attachment.modifier.AdsModifier;
import xiao.customgun.core.item.attachment.modifier.*;
import xiao.customgun.core.resource.data.data.attachment.*;
import xiao.customgun.core.resource.data.data.gun._InaccuracyData;

import java.util.HashMap;
import java.util.Map;

public enum AttachmentModifierType implements ResourceTag.CategoryTag, IGunModifierType {
    // 瞄准速度
    ADS(GunModifierType.ADS,
            AdsModifier.INSTANCE),

    // 子弹属性
    DAMAGE_CALCULATION(GunModifierType.DAMAGE_CALCULATION,
            DamageCalculationModifier.INSTANCE),
    HEADSHOT_MULTIPLIER(GunModifierType.HEADSHOT_MULTIPLIER,
            HeadshotMultiplierModifier.INSTANCE),
    ARMOR_IGNORE_PERCENT(GunModifierType.ARMOR_IGNORE_PERCENT,
            ArmorIgnoreModifier.INSTANCE),
    BULLET_SPEED(GunModifierType.BULLET_SPEED,
            BulletSpeedModifier.INSTANCE),
    PIERCE_COUNT(GunModifierType.PIERCE_COUNT,
            PierceCountModifier.INSTANCE),
    FIRE_ASPECT(GunModifierType.FIRE_ASPECT,
            FireAspectModifier.INSTANCE),
    KNOCKBACK_STRENGTH(GunModifierType.KNOCKBACK_STRENGTH,
            KnockbackStrengthModifier.INSTANCE),
    BULLET_EXPLOSION(GunModifierType.BULLET_EXPLOSION,
            BulletExplosionModifier.INSTANCE),

    // 枪械属性
    RPM(GunModifierType.RPM,
            RpmModifier.INSTANCE),
    RECOIL_DATA(GunModifierType.RECOIL_DATA,
            RecoilDataModifier.INSTANCE),
    EFFECTIVE_RANGE(GunModifierType.EFFECTIVE_RANGE,
            EffectiveRangeModifier.INSTANCE),
    WEIGHT(GunModifierType.WEIGHT,
            WeightModifier.INSTANCE),
    MUZZLE(GunModifierType.MUZZLE,
            MuzzleModifier.INSTANCE),
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    AIM_INACCURACY(GunModifierType.AIM_INACCURACY,
            AimInaccuracyModifier.INSTANCE),
    SNEAK_INACCURACY(GunModifierType.SNEAK_INACCURACY,
            SneakInaccuracyModifier.INSTANCE),
    PRONE_INACCURACY(GunModifierType.PRONE_INACCURACY,
            ProneInaccuracyModifier.INSTANCE),
    OTHER_INACCURACY(GunModifierType.OTHER_INACCURACY,
            OtherInaccuracyModifier.INSTANCE),
    // 近战
    MELEE(GunModifierType.MELEE,
            MeleeModifier.INSTANCE),

    // 弹匣
    MAGAZINE_CATEGORY(GunModifierType.MAGAZINE_CATEGORY,
            MagazineCategoryModifier.INSTANCE);

    public final GunModifierType modifierType;
    public final String typeName;
    public final IAttachmentModifier<?, ?> modifier;
    <K, V> AttachmentModifierType(GunModifierType type, IAttachmentModifier<K, V> modifier) {
        this.modifierType = type;
        this.typeName = type.typeName;
        this.modifier = modifier;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    @Override
    public GunModifierType getGunModifierType() {
        return this.modifierType;
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