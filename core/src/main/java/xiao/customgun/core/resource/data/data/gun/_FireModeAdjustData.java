/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._FireModeAdjustDataTag;
import xiao.customgun.core.api.resource.data.data.gun._InaccuracyDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _FireModeAdjustData extends ResourcePojo<_FireModeAdjustData> {

    private int rpm = 0;

    private float damage = 0;
    private float bulletSpeed = 0;
    private float knockbackStrength = 0;

    private float armorIgnorePercent = 0;
    private float headshotMultiplier = 0;

    /**
     * {@link _InaccuracyDataTag}
     */
    private float aimInaccuracy = 0;
    private float otherInaccuracy = 0;

    private static final _FireModeAdjustData PARSER = new _FireModeAdjustData();
    public static _FireModeAdjustData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _FireModeAdjustData fromJsonReader(JsonReader reader) throws IOException {
        _FireModeAdjustData pojo = new _FireModeAdjustData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FireModeAdjustDataTag.RPM -> pojo.rpm = JsonUtils.readInt(reader);

                    case _FireModeAdjustDataTag.DAMAGE -> pojo.damage = JsonUtils.readFloat(reader);
                    case _FireModeAdjustDataTag.BULLET_SPEED, _FireModeAdjustDataTag.BULLET_SPEED_OLD1 -> pojo.bulletSpeed = JsonUtils.readFloat(reader);
                    case _FireModeAdjustDataTag.KNOCKBACK_STRENGTH, _FireModeAdjustDataTag.KNOCKBACK_STRENGTH_OLD1 -> pojo.knockbackStrength = JsonUtils.readFloat(reader);

                    case _FireModeAdjustDataTag.ARMOR_IGNORE_PERCENT, _FireModeAdjustDataTag.ARMOR_IGNORE_PERCENT_OLD1 -> pojo.armorIgnorePercent = JsonUtils.readFloat(reader);
                    case _FireModeAdjustDataTag.HEADSHOT_MULTIPLIER, _FireModeAdjustDataTag.HEADSHOT_MULTIPLIER_OLD1 -> pojo.headshotMultiplier = JsonUtils.readFloat(reader);

                    case _FireModeAdjustDataTag.AIM_INACCURACY -> pojo.aimInaccuracy = JsonUtils.readFloat(reader);
                    case _FireModeAdjustDataTag.OTHER_INACCURACY -> pojo.otherInaccuracy = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FireModeAdjustData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeInt(writer, _FireModeAdjustDataTag.RPM, rpm);

            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.DAMAGE, damage);
            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.BULLET_SPEED, bulletSpeed);
            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.KNOCKBACK_STRENGTH, knockbackStrength);

            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.ARMOR_IGNORE_PERCENT, armorIgnorePercent);
            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.HEADSHOT_MULTIPLIER, headshotMultiplier);

            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.AIM_INACCURACY, aimInaccuracy);
            JsonUtils.writeFloat(writer, _FireModeAdjustDataTag.OTHER_INACCURACY, otherInaccuracy);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public int getRpm() {
        return rpm;
    }
    public float getDamage() {
        return damage;
    }
    public float getBulletSpeed() {
        return bulletSpeed;
    }
    public float getKnockbackStrength() {
        return knockbackStrength;
    }
    public float getArmorIgnorePercent() {
        return armorIgnorePercent;
    }
    public float getHeadshotMultiplier() {
        return headshotMultiplier;
    }
    public float getAimInaccuracy() {
        return aimInaccuracy;
    }
    public float getOtherInaccuracy() {
        return otherInaccuracy;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }
    public void setDamage(float damage) {
        this.damage = damage;
    }
    public void setBulletSpeed(float bulletSpeed) {
        this.bulletSpeed = bulletSpeed;
    }
    public void setKnockbackStrength(float knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }
    public void setArmorIgnorePercent(float armorIgnorePercent) {
        this.armorIgnorePercent = armorIgnorePercent;
    }
    public void setHeadshotMultiplier(float headshotMultiplier) {
        this.headshotMultiplier = headshotMultiplier;
    }
    public void setAimInaccuracy(float aimInaccuracy) {
        this.aimInaccuracy = aimInaccuracy;
    }
    public void setOtherInaccuracy(float otherInaccuracy) {
        this.otherInaccuracy = otherInaccuracy;
    }
}