/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._FireModeDataTag;
import xiao.customgun.core.api.resource.data.data.gun._InaccuracyDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _FireModeData extends ResourcePojo<_FireModeData> {

    private int rpm = 300;

    private float damage = 0;
    private float bulletSpeed = 5f;
    private float knockbackStrength = 0;

    private float armorIgnorePercent = 0.0F;
    private float headshotMultiplier = 1.0F;

    /**
     * {@link _InaccuracyDataTag}
     */
    private float aimInaccuracy = 0.0F;
    private float otherInaccuracy = 0.0F;

    private static final _FireModeData PARSER = new _FireModeData();
    public static _FireModeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _FireModeData fromJsonReader(JsonReader reader) throws IOException {
        _FireModeData pojo = new _FireModeData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FireModeDataTag.RPM -> pojo.rpm = JsonUtils.readInt(reader);

                    case _FireModeDataTag.DAMAGE -> pojo.damage = JsonUtils.readFloat(reader);
                    case _FireModeDataTag.BULLET_SPEED -> pojo.bulletSpeed = JsonUtils.readFloat(reader);
                    case _FireModeDataTag.KNOCKBACK_STRENGTH -> pojo.knockbackStrength = JsonUtils.readFloat(reader);

                    case _FireModeDataTag.ARMOR_IGNORE_PERCENT -> pojo.armorIgnorePercent = JsonUtils.readFloat(reader);
                    case _FireModeDataTag.HEADSHOT_MULTIPLIER -> pojo.headshotMultiplier = JsonUtils.readFloat(reader);

                    case _FireModeDataTag.AIM_INACCURACY -> pojo.aimInaccuracy = JsonUtils.readFloat(reader);
                    case _FireModeDataTag.OTHER_INACCURACY -> pojo.otherInaccuracy = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FireModeData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeInt(writer, _FireModeDataTag.RPM, rpm);

            JsonUtils.writeFloat(writer, _FireModeDataTag.DAMAGE, damage);
            JsonUtils.writeFloat(writer, _FireModeDataTag.BULLET_SPEED, bulletSpeed);
            JsonUtils.writeFloat(writer, _FireModeDataTag.KNOCKBACK_STRENGTH, knockbackStrength);

            JsonUtils.writeFloat(writer, _FireModeDataTag.ARMOR_IGNORE_PERCENT, armorIgnorePercent);
            JsonUtils.writeFloat(writer, _FireModeDataTag.HEADSHOT_MULTIPLIER, headshotMultiplier);

            JsonUtils.writeFloat(writer, _FireModeDataTag.AIM_INACCURACY, aimInaccuracy);
            JsonUtils.writeFloat(writer, _FireModeDataTag.OTHER_INACCURACY, otherInaccuracy);
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