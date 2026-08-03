/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.attachment._MeleeModifierDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.attachment.melee._TargetEffectData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class _MeleeModifierData extends ResourcePojo<_MeleeModifierData> {

    // 近战属性
    private float meleeDamage = 0.0F;
    private float meleeDistance = 1.0F;
    private float rangeAngle = 30.0F;

    // 时间属性
    private float damageDelaySeconds = 0.1F;
    private float extraCooldown = 0.0F;

    // 命中效果
    private float knockbackStrength = 0.0F;
    private List<_TargetEffectData> targetEffect;

    private static final _MeleeModifierData PARSER = new _MeleeModifierData();
    public static _MeleeModifierData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _MeleeModifierData fromJsonReader(JsonReader reader) throws IOException {
        _MeleeModifierData pojo = new _MeleeModifierData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _MeleeModifierDataTag.MELEE_DAMAGE, _MeleeModifierDataTag.MELEE_DAMAGE_OLD1 -> pojo.meleeDamage = JsonUtils.readFloat(reader);
                    case _MeleeModifierDataTag.MELEE_DISTANCE, _MeleeModifierDataTag.MELEE_DISTANCE_OLD1 -> pojo.meleeDistance = JsonUtils.readFloat(reader);
                    case _MeleeModifierDataTag.RANGE_ANGLE -> pojo.rangeAngle = JsonUtils.readFloat(reader);

                    case _MeleeModifierDataTag.DAMAGE_DELAY_SECONDS, _MeleeModifierDataTag.DAMAGE_DELAY_SECONDS_OLD1 -> pojo.damageDelaySeconds = JsonUtils.readFloat(reader);
                    case _MeleeModifierDataTag.BASE_COOLDOWN, _MeleeModifierDataTag.BASE_COOLDOWN_OLD1 -> pojo.extraCooldown = JsonUtils.readFloat(reader);

                    case _MeleeModifierDataTag.KNOCKBACK_STRENGTH, _MeleeModifierDataTag.KNOCKBACK_STRENGTH_OLD1 -> pojo.knockbackStrength = JsonUtils.readFloat(reader);
                    case _MeleeModifierDataTag.TARGET_EFFECT, _MeleeModifierDataTag.TARGET_EFFECT_OLD1 -> pojo.targetEffect = JsonUtils.readList(reader, _TargetEffectData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MeleeModifierData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _MeleeModifierDataTag.MELEE_DAMAGE, this.meleeDamage);
            JsonUtils.writeFloat(writer, _MeleeModifierDataTag.MELEE_DISTANCE, this.meleeDistance);
            JsonUtils.writeFloat(writer, _MeleeModifierDataTag.RANGE_ANGLE, this.rangeAngle);

            JsonUtils.writeFloat(writer, _MeleeModifierDataTag.DAMAGE_DELAY_SECONDS, this.damageDelaySeconds);
            JsonUtils.writeFloat(writer, _MeleeModifierDataTag.BASE_COOLDOWN, this.extraCooldown);

            JsonUtils.writeFloat(writer, _MeleeModifierDataTag.KNOCKBACK_STRENGTH, this.knockbackStrength);
            JsonUtils.writeList(writer, _MeleeModifierDataTag.TARGET_EFFECT, this.targetEffect, _TargetEffectData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        boolean n1 = (this.targetEffect == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        int size = this.targetEffect.size();
        for (int i = 0; i < size; i++) {
            var data = this.targetEffect.get(i);
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }

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
    public float getExtraCooldown() {
        return extraCooldown;
    }
    public float getKnockbackStrength() {
        return knockbackStrength;
    }
    public List<_TargetEffectData> getTargetEffect() {
        return targetEffect;
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
    public void setExtraCooldown(float extraCooldown) {
        this.extraCooldown = extraCooldown;
    }
    public void setKnockbackStrength(float knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }
    public void setTargetEffect(List<_TargetEffectData> targetEffect) {
        this.targetEffect = targetEffect;
    }

    // --------Back compatibility--------

    @Override
    public _MeleeModifierData applyBackCompatibility() {
        if (this.targetEffect == null) this.targetEffect = new ArrayList<>();
        else {
            int size = 0;
            for (int i = 0; i < size; i++) this.targetEffect.get(i).applyBackCompatibility();
        }
        return this;
    }
}