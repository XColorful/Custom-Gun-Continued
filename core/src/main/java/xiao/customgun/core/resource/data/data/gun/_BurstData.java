/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.GunDataTag;
import xiao.customgun.core.api.resource.data.data.gun._BulletDataTag;
import xiao.customgun.core.api.resource.data.data.gun._BurstDataTag;
import xiao.customgun.core.api.resource.data.data.gun._InaccuracyDataTag;
import xiao.customgun.core.api.resource.data.data.gun.bullet._ExtraBulletDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _BurstData extends ResourcePojo<_BurstData> {

    /**
     * {@link GunDataTag}
     */
    private int rpm = 0;

    /**
     * {@link _BulletDataTag}
     */
    private float damage = 0.0F;
    private float bulletSpeed = 0.0F;
    private float knockbackStrength = 0.0F;

    /**
     * {@link _ExtraBulletDataTag}
     */
    private float armorIgnorePercent = 0.0F;
    private float headshotMultiplier = 0.0F;

    /**
     * {@link _InaccuracyDataTag}
     */
    private float aimInaccuracy = 0.0F;
    private float otherInaccuracy = 0.0F;

    private static final _BurstData PARSER = new _BurstData();
    public static _BurstData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _BurstData fromJsonReader(JsonReader reader) throws IOException {
        _BurstData pojo = new _BurstData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BurstDataTag.RPM -> pojo.rpm = JsonUtils.readInt(reader);

                    case _BurstDataTag.DAMAGE -> pojo.damage = JsonUtils.readFloat(reader);
                    case _BurstDataTag.BULLET_SPEED -> pojo.bulletSpeed = JsonUtils.readFloat(reader);
                    case _BurstDataTag.KNOCKBACK_STRENGTH -> pojo.knockbackStrength = JsonUtils.readFloat(reader);

                    case _BurstDataTag.ARMOR_IGNORE_PERCENT -> pojo.armorIgnorePercent = JsonUtils.readFloat(reader);
                    case _BurstDataTag.HEADSHOT_MULTIPLIER -> pojo.headshotMultiplier = JsonUtils.readFloat(reader);

                    case _BurstDataTag.AIM_INACCURACY -> pojo.aimInaccuracy = JsonUtils.readFloat(reader);
                    case _BurstDataTag.OTHER_INACCURACY -> pojo.otherInaccuracy = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _BurstData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeInt(writer, _BurstDataTag.RPM, this.rpm);

            JsonUtils.writeFloat(writer, _BurstDataTag.DAMAGE, this.damage);
            JsonUtils.writeFloat(writer, _BurstDataTag.BULLET_SPEED, this.bulletSpeed);

            JsonUtils.writeFloat(writer, _BurstDataTag.KNOCKBACK_STRENGTH, this.knockbackStrength);
            JsonUtils.writeFloat(writer, _BurstDataTag.ARMOR_IGNORE_PERCENT, this.armorIgnorePercent);
            JsonUtils.writeFloat(writer, _BurstDataTag.HEADSHOT_MULTIPLIER, this.headshotMultiplier);

            JsonUtils.writeFloat(writer, _BurstDataTag.AIM_INACCURACY, this.aimInaccuracy);
            JsonUtils.writeFloat(writer, _BurstDataTag.OTHER_INACCURACY, this.otherInaccuracy);
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