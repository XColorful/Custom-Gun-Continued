/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.attachment.MagazineCategory;
import xiao.customgun.core.api.resource.data.data.AttachmentDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.attachment.*;
import xiao.customgun.core.resource.data.data.gun._InaccuracyData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class AttachmentData extends ResourcePojo<AttachmentData> {

    // 瞄准速度
    private _SimpleModifierData adsModifier;

    // 子弹属性
    private _SimpleModifierData armorIgnorePercentModifier;
    private _SimpleModifierData headshotMultiplierModifier;
    private _SimpleModifierData damageCalculationModifier;
    private _SimpleModifierData bulletSpeedModifier;
    private _SimpleModifierData pierceCountModifier;
    private _FireAspectModifierData fireAspectModifier;
    private _SimpleModifierData knockbackStrengthModifier;
    private _BulletExplosionModifierData bulletExplosionModifier;

    // 枪械属性
    private _SimpleModifierData rpmModifier;
    private _RecoilDataModifierData recoilDataModifier;
    private _SimpleModifierData effectiveRangeModifier;
    private float weight = 0.0F;
    private _MuzzleModifierData muzzleModifier;
    /**
     * 不准确度Modifier {@link _InaccuracyData}
     */
    private _SimpleModifierData aimInaccuracyModifier;
    private _SimpleModifierData sneakInaccuracyModifier;
    private _SimpleModifierData proneInaccuracyModifier;
    private _SimpleModifierData otherInaccuracyModifier;
    // 近战
    private _MeleeModifierData meleeModifier;

    // 弹匣
    private MagazineCategory magazineCategory;

    private static final AttachmentData PARSER = new AttachmentData();
    public static AttachmentData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AttachmentData fromJsonReader(JsonReader reader) throws IOException {
        AttachmentData pojo = new AttachmentData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AttachmentDataTag.ADS -> pojo.adsModifier = _SimpleModifierData.fromJson(reader);

                    case AttachmentDataTag.ARMOR_IGNORE_PERCENT, AttachmentDataTag.ARMOR_IGNORE_PERCENT_OLD1 -> pojo.armorIgnorePercentModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.HEADSHOT_MULTIPLIER, AttachmentDataTag.HEADSHOT_MULTIPLIER_OLD1 -> pojo.headshotMultiplierModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.DAMAGE_CALCULATION, AttachmentDataTag.DAMAGE_CALCULATION_OLD1 -> pojo.damageCalculationModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.BULLET_SPEED, AttachmentDataTag.BULLET_SPEED_OLD1 -> pojo.bulletSpeedModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.PIERCE_COUNT, AttachmentDataTag.PIERCE_COUNT_OLD1 -> pojo.pierceCountModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.FIRE_ASPECT, AttachmentDataTag.FIRE_ASPECT_OLD1 -> pojo.fireAspectModifier = _FireAspectModifierData.fromJson(reader);
                    case AttachmentDataTag.KNOCKBACK_STRENGTH, AttachmentDataTag.KNOCKBACK_STRENGTH_OLD1 -> pojo.knockbackStrengthModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.BULLET_EXPLOSION, AttachmentDataTag.BULLET_EXPLOSION_OLD1 -> pojo.bulletExplosionModifier = _BulletExplosionModifierData.fromJson(reader);

                    case AttachmentDataTag.RPM -> pojo.rpmModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.RECOIL_DATA, AttachmentDataTag.RECOIL_DATA_OLD1 -> pojo.recoilDataModifier = _RecoilDataModifierData.fromJson(reader);
                    case AttachmentDataTag.EFFECTIVE_RANGE -> pojo.effectiveRangeModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.WEIGHT -> pojo.weight = JsonUtils.readFloat(reader);
                    case AttachmentDataTag.MUZZLE, AttachmentDataTag.MUZZLE_OLD1 -> pojo.muzzleModifier = _MuzzleModifierData.fromJson(reader);

                    case AttachmentDataTag.AIM_INACCURACY -> pojo.aimInaccuracyModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.SNEAK_INACCURACY -> pojo.sneakInaccuracyModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.PRONE_INACCURACY, AttachmentDataTag.PRONE_INACCURACY_OLD1 -> pojo.proneInaccuracyModifier = _SimpleModifierData.fromJson(reader);
                    case AttachmentDataTag.OTHER_INACCURACY, AttachmentDataTag.OTHER_INACCURACY_OLD1 -> pojo.otherInaccuracyModifier = _SimpleModifierData.fromJson(reader);

                    case AttachmentDataTag.MELEE -> pojo.meleeModifier = _MeleeModifierData.fromJson(reader);

                    case AttachmentDataTag.MAGAZINE_CATEGORY -> pojo.magazineCategory = JsonUtils.readFromString(reader, MagazineCategory::fromString);
                    case AttachmentDataTag.MAGAZINE_CATEGORY_OLD1 -> pojo.magazineCategory = MagazineCategory.fromIndex(JsonUtils.readInt(reader));
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AttachmentData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, AttachmentDataTag.ADS, this.adsModifier, _SimpleModifierData::toJson);

            JsonUtils.write(writer, AttachmentDataTag.ARMOR_IGNORE_PERCENT, this.armorIgnorePercentModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.HEADSHOT_MULTIPLIER, this.headshotMultiplierModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.DAMAGE_CALCULATION, this.damageCalculationModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.BULLET_SPEED, this.bulletSpeedModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.PIERCE_COUNT, this.pierceCountModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.FIRE_ASPECT, this.fireAspectModifier, _FireAspectModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.KNOCKBACK_STRENGTH, this.knockbackStrengthModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.BULLET_EXPLOSION, this.bulletExplosionModifier, _BulletExplosionModifierData::toJson);

            JsonUtils.write(writer, AttachmentDataTag.RPM, this.rpmModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.RECOIL_DATA, this.recoilDataModifier, _RecoilDataModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.EFFECTIVE_RANGE, this.effectiveRangeModifier, _SimpleModifierData::toJson);
            JsonUtils.writeFloat(writer, AttachmentDataTag.WEIGHT, this.weight);
            JsonUtils.write(writer, AttachmentDataTag.MUZZLE, this.muzzleModifier, _MuzzleModifierData::toJson);

            JsonUtils.write(writer, AttachmentDataTag.AIM_INACCURACY, this.aimInaccuracyModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.SNEAK_INACCURACY, this.sneakInaccuracyModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.PRONE_INACCURACY, this.proneInaccuracyModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, AttachmentDataTag.OTHER_INACCURACY, this.otherInaccuracyModifier, _SimpleModifierData::toJson);

            JsonUtils.write(writer, AttachmentDataTag.MELEE, this.meleeModifier, _MeleeModifierData::toJson);

            JsonUtils.writeToString(writer, AttachmentDataTag.MAGAZINE_CATEGORY, this.magazineCategory);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public _SimpleModifierData getAdsModifier() {
        return adsModifier;
    }
    public _SimpleModifierData getArmorIgnorePercentModifier() {
        return armorIgnorePercentModifier;
    }
    public _SimpleModifierData getHeadshotMultiplierModifier() {
        return headshotMultiplierModifier;
    }
    public _SimpleModifierData getDamageCalculationModifier() {
        return damageCalculationModifier;
    }
    public _SimpleModifierData getBulletSpeedModifier() {
        return bulletSpeedModifier;
    }
    public _SimpleModifierData getPierceCountModifier() {
        return pierceCountModifier;
    }
    public _FireAspectModifierData getFireAspectModifier() {
        return fireAspectModifier;
    }
    public _SimpleModifierData getKnockbackStrengthModifier() {
        return knockbackStrengthModifier;
    }
    public _BulletExplosionModifierData getBulletExplosionModifier() {
        return bulletExplosionModifier;
    }
    public _SimpleModifierData getRpmModifier() {
        return rpmModifier;
    }
    public _RecoilDataModifierData getRecoilDataModifier() {
        return recoilDataModifier;
    }
    public _SimpleModifierData getEffectiveRangeModifier() {
        return effectiveRangeModifier;
    }
    public float getWeight() {
        return weight;
    }
    public _MuzzleModifierData getMuzzleModifier() {
        return muzzleModifier;
    }
    public _SimpleModifierData getAimInaccuracyModifier() {
        return aimInaccuracyModifier;
    }
    public _SimpleModifierData getSneakInaccuracyModifier() {
        return sneakInaccuracyModifier;
    }
    public _SimpleModifierData getProneInaccuracyModifier() {
        return proneInaccuracyModifier;
    }
    public _SimpleModifierData getOtherInaccuracyModifier() {
        return otherInaccuracyModifier;
    }
    public _MeleeModifierData getMeleeModifier() {
        return meleeModifier;
    }
    public MagazineCategory getMagazineCategory() {
        return magazineCategory;
    }

    public void setAdsModifier(_SimpleModifierData adsModifier) {
        this.adsModifier = adsModifier;
    }
    public void setArmorIgnorePercentModifier(_SimpleModifierData armorIgnorePercentModifier) {
        this.armorIgnorePercentModifier = armorIgnorePercentModifier;
    }
    public void setHeadshotMultiplierModifier(_SimpleModifierData headshotMultiplierModifier) {
        this.headshotMultiplierModifier = headshotMultiplierModifier;
    }
    public void setDamageCalculationModifier(_SimpleModifierData damageCalculationModifier) {
        this.damageCalculationModifier = damageCalculationModifier;
    }
    public void setBulletSpeedModifier(_SimpleModifierData bulletSpeedModifier) {
        this.bulletSpeedModifier = bulletSpeedModifier;
    }
    public void setPierceCountModifier(_SimpleModifierData pierceCountModifier) {
        this.pierceCountModifier = pierceCountModifier;
    }
    public void setFireAspectModifier(_FireAspectModifierData fireAspectModifier) {
        this.fireAspectModifier = fireAspectModifier;
    }
    public void setKnockbackStrengthModifier(_SimpleModifierData knockbackStrengthModifier) {
        this.knockbackStrengthModifier = knockbackStrengthModifier;
    }
    public void setBulletExplosionModifier(_BulletExplosionModifierData bulletExplosionModifier) {
        this.bulletExplosionModifier = bulletExplosionModifier;
    }
    public void setRpmModifier(_SimpleModifierData rpmModifier) {
        this.rpmModifier = rpmModifier;
    }
    public void setRecoilDataModifier(_RecoilDataModifierData recoilDataModifier) {
        this.recoilDataModifier = recoilDataModifier;
    }
    public void setEffectiveRangeModifier(_SimpleModifierData effectiveRangeModifier) {
        this.effectiveRangeModifier = effectiveRangeModifier;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setMuzzleModifier(_MuzzleModifierData muzzleModifier) {
        this.muzzleModifier = muzzleModifier;
    }
    public void setAimInaccuracyModifier(_SimpleModifierData aimInaccuracyModifier) {
        this.aimInaccuracyModifier = aimInaccuracyModifier;
    }
    public void setSneakInaccuracyModifier(_SimpleModifierData sneakInaccuracyModifier) {
        this.sneakInaccuracyModifier = sneakInaccuracyModifier;
    }
    public void setProneInaccuracyModifier(_SimpleModifierData proneInaccuracyModifier) {
        this.proneInaccuracyModifier = proneInaccuracyModifier;
    }
    public void setOtherInaccuracyModifier(_SimpleModifierData otherInaccuracyModifier) {
        this.otherInaccuracyModifier = otherInaccuracyModifier;
    }
    public void setMeleeModifier(_MeleeModifierData meleeModifier) {
        this.meleeModifier = meleeModifier;
    }
    public void setMagazineCategory(MagazineCategory magazineCategory) {
        this.magazineCategory = magazineCategory;
    }
}