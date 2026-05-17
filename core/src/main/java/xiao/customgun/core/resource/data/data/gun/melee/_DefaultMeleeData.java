/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun.melee;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun.melee._DefaultMeleeDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _DefaultMeleeData extends ResourcePojo<_DefaultMeleeData> {

    // 近战属性
    private float meleeDamage = 0.0F;
    private float meleeDistance = 1.0F;
    private float rangeAngle = 30.0F;

    // 时间属性
    private float damageDelaySeconds = 0.1F;
    private float baseCooldown = 0.0F;

    // 命中效果
    private float knockbackStrength = 0.2F;

    // 显示
    private String animationType;

    private static final _DefaultMeleeData PARSER = new _DefaultMeleeData();
    public static _DefaultMeleeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _DefaultMeleeData fromJsonReader(JsonReader reader) throws IOException {
        _DefaultMeleeData pojo = new _DefaultMeleeData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _DefaultMeleeDataTag.MELEE_DAMAGE, _DefaultMeleeDataTag.MELEE_DAMAGE_OLD1 -> pojo.meleeDamage = JsonUtils.readFloat(reader);
                    case _DefaultMeleeDataTag.MELEE_DISTANCE, _DefaultMeleeDataTag.MELEE_DISTANCE_OLD1 -> pojo.meleeDistance = JsonUtils.readFloat(reader);
                    case _DefaultMeleeDataTag.RANGE_ANGLE -> pojo.rangeAngle = JsonUtils.readFloat(reader);

                    case _DefaultMeleeDataTag.DAMAGE_DELAY_SECONDS, _DefaultMeleeDataTag.DAMAGE_DELAY_SECONDS_OLD1 -> pojo.damageDelaySeconds = JsonUtils.readFloat(reader);
                    case _DefaultMeleeDataTag.BASE_COOLDOWN, _DefaultMeleeDataTag.BASE_COOLDOWN_OLD1 -> pojo.baseCooldown = JsonUtils.readFloat(reader);

                    case _DefaultMeleeDataTag.KNOCKBACK_STRENGTH, _DefaultMeleeDataTag.KNOCKBACK_STRENGTH_OLD1 -> pojo.knockbackStrength = JsonUtils.readFloat(reader);

                    case _DefaultMeleeDataTag.ANIMATION_TYPE -> pojo.animationType = JsonUtils.readString(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _DefaultMeleeData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _DefaultMeleeDataTag.MELEE_DAMAGE, this.meleeDamage);
            JsonUtils.writeFloat(writer, _DefaultMeleeDataTag.MELEE_DISTANCE, this.meleeDistance);
            JsonUtils.writeFloat(writer, _DefaultMeleeDataTag.RANGE_ANGLE, this.rangeAngle);

            JsonUtils.writeFloat(writer, _DefaultMeleeDataTag.DAMAGE_DELAY_SECONDS, this.damageDelaySeconds);
            JsonUtils.writeFloat(writer, _DefaultMeleeDataTag.BASE_COOLDOWN, this.baseCooldown);

            JsonUtils.writeFloat(writer, _DefaultMeleeDataTag.KNOCKBACK_STRENGTH, this.knockbackStrength);

            JsonUtils.writeString(writer, _DefaultMeleeDataTag.ANIMATION_TYPE, this.animationType);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getMeleeDamage() {
        return meleeDamage;
    }
    public float getMeleeDistance() {
        return meleeDistance;
    }
    public float getRangeAngle() {
        return rangeAngle;
    }
    public float getDamageDelaySeconds() {
        return damageDelaySeconds;
    }
    public float getBaseCooldown() {
        return baseCooldown;
    }
    public float getKnockbackStrength() {
        return knockbackStrength;
    }
    public String getAnimationType() {
        return animationType;
    }

    public void setMeleeDamage(float meleeDamage) {
        this.meleeDamage = meleeDamage;
    }
    public void setMeleeDistance(float meleeDistance) {
        this.meleeDistance = meleeDistance;
    }
    public void setRangeAngle(float rangeAngle) {
        this.rangeAngle = rangeAngle;
    }
    public void setDamageDelaySeconds(float damageDelaySeconds) {
        this.damageDelaySeconds = damageDelaySeconds;
    }
    public void setBaseCooldown(float baseCooldown) {
        this.baseCooldown = baseCooldown;
    }
    public void setKnockbackStrength(float knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }
    public void setAnimationType(String animationType) {
        this.animationType = animationType;
    }
}