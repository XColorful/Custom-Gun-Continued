/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun.bullet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun.bullet._BulletSkillDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public class _BulletSkillData extends ResourcePojo<_BulletSkillData> {

    private float armorIgnorePercent = 0.0F;
    private float headshotMultiplier = 1.0F;
    private List<_DistanceDamageData> damageCalculation;

    private static final _BulletSkillData PARSER = new _BulletSkillData();
    public static _BulletSkillData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _BulletSkillData fromJsonReader(JsonReader reader) throws IOException {
        _BulletSkillData pojo = new _BulletSkillData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BulletSkillDataTag.ARMOR_IGNORE_PERCENT -> pojo.armorIgnorePercent = JsonUtils.readFloat(reader);
                    case _BulletSkillDataTag.HEADSHOT_MULTIPLIER -> pojo.headshotMultiplier = JsonUtils.readFloat(reader);
                    case _BulletSkillDataTag.DAMAGE_CALCULATION -> pojo.damageCalculation = JsonUtils.readList(reader, _DistanceDamageData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _BulletSkillData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _BulletSkillDataTag.ARMOR_IGNORE_PERCENT, this.armorIgnorePercent);
            JsonUtils.writeFloat(writer, _BulletSkillDataTag.HEADSHOT_MULTIPLIER, this.headshotMultiplier);
            JsonUtils.writeList(writer, _BulletSkillDataTag.DAMAGE_CALCULATION, this.damageCalculation, _DistanceDamageData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getArmorIgnorePercent() {
        return armorIgnorePercent;
    }
    public float getHeadshotMultiplier() {
        return headshotMultiplier;
    }
    public List<_DistanceDamageData> getDamageCalculation() {
        return damageCalculation;
    }

    public void setArmorIgnorePercent(float armorIgnorePercent) {
        this.armorIgnorePercent = armorIgnorePercent;
    }
    public void setHeadshotMultiplier(float headshotMultiplier) {
        this.headshotMultiplier = headshotMultiplier;
    }
    public void setDamageCalculation(List<_DistanceDamageData> damageCalculation) {
        this.damageCalculation = damageCalculation;
    }
}