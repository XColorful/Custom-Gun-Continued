/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment.*;
import xiao.customgun.core.resource.data.data.gun._InaccuracyData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum AttachmentModifierType implements ResourceTag.CategoryTag {
    // 瞄准速度
    ADS(AttachmentModifierTypeTag.ADS,
            _SimpleModifierData.class, AttachmentData::getAdsModifier),

    // 子弹属性
    DAMAGE_CALCULATION(AttachmentModifierTypeTag.DAMAGE_CALCULATION,
            _SimpleModifierData.class, AttachmentData::getDamageCalculationModifier),
    HEADSHOT_MULTIPLIER(AttachmentModifierTypeTag.HEADSHOT_MULTIPLIER,
            _SimpleModifierData.class, AttachmentData::getHeadshotMultiplierModifier),
    ARMOR_IGNORE_PERCENT(AttachmentModifierTypeTag.ARMOR_IGNORE_PERCENT,
            _SimpleModifierData.class, AttachmentData::getArmorIgnorePercentModifier),
    BULLET_SPEED(AttachmentModifierTypeTag.BULLET_SPEED,
            _SimpleModifierData.class, AttachmentData::getBulletSpeedModifier),
    PIERCE_COUNT(AttachmentModifierTypeTag.PIERCE_COUNT,
            _SimpleModifierData.class, AttachmentData::getPierceCountModifier),
    FIRE_ASPECT(AttachmentModifierTypeTag.FIRE_ASPECT,
            _FireAspectModifierData.class, AttachmentData::getFireAspectModifier),
    KNOCKBACK_STRENGTH(AttachmentModifierTypeTag.KNOCKBACK_STRENGTH,
            _SimpleModifierData.class, AttachmentData::getKnockbackStrengthModifier),
    BULLET_EXPLOSION(AttachmentModifierTypeTag.BULLET_EXPLOSION,
            _BulletExplosionModifierData.class, AttachmentData::getBulletExplosionModifier),

    // 枪械属性
    RPM(AttachmentModifierTypeTag.RPM,
            _SimpleModifierData.class, AttachmentData::getRpmModifier),
    RECOIL_DATA(AttachmentModifierTypeTag.RECOIL_DATA,
            _RecoilDataModifierData.class, AttachmentData::getRecoilDataModifier),
    EFFECTIVE_RANGE(AttachmentModifierTypeTag.EFFECTIVE_RANGE,
            _SimpleModifierData.class, AttachmentData::getEffectiveRangeModifier),
    WEIGHT(AttachmentModifierTypeTag.WEIGHT,
            Float.class, AttachmentData::getWeight),
    MUZZLE(AttachmentModifierTypeTag.MUZZLE,
            _MuzzleModifierData.class, AttachmentData::getMuzzleModifier),
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    AIM_INACCURACY(AttachmentModifierTypeTag.AIM_INACCURACY,
            _SimpleModifierData.class, AttachmentData::getAimInaccuracyModifier),
    SNEAK_INACCURACY(AttachmentModifierTypeTag.SNEAK_INACCURACY,
            _SimpleModifierData.class, AttachmentData::getSneakInaccuracyModifier),
    PRONE_INACCURACY(AttachmentModifierTypeTag.PRONE_INACCURACY,
            _SimpleModifierData.class, AttachmentData::getProneInaccuracyModifier),
    OTHER_INACCURACY(AttachmentModifierTypeTag.OTHER_INACCURACY,
            _SimpleModifierData.class, AttachmentData::getOtherInaccuracyModifier),
    // 近战
    MELEE(AttachmentModifierTypeTag.MELEE,
            _MeleeModifierData.class, AttachmentData::getMeleeModifier),

    // 弹匣
    MAGAZINE_CATEGORY(AttachmentModifierTypeTag.MAGAZINE_CATEGORY,
            MagazineCategory.class, AttachmentData::getMagazineCategory);


    public final String typeName;
    public final Class<?> dataType;
    public final Function<AttachmentData, ?> getter;
    <T> AttachmentModifierType(String name, Class<T> dataType, Function<AttachmentData, T> getter) {
        this.typeName = name;
        this.dataType = dataType;
        this.getter = getter;
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

    public @Nullable <T> T get(AttachmentData data, Class<T> clazz) {
        if (!clazz.isAssignableFrom(this.dataType)) {
            throw new IllegalArgumentException("Invalid modifier data type: " + clazz.getName());
        }

        Object value = getter.apply(data);
        return value != null ? clazz.cast(value) : null;
    }
}