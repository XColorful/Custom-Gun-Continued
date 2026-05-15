/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun.bullet.damage;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun.bullet.damage._DistanceDamageDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _DistanceDamageData extends ResourcePojo<_DistanceDamageData> {

    private float distance = 0;
    private float damage = 0;

    private static final _DistanceDamageData PARSER = new _DistanceDamageData();
    public static _DistanceDamageData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _DistanceDamageData fromJsonReader(JsonReader reader) throws IOException {
        _DistanceDamageData pojo = new _DistanceDamageData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _DistanceDamageDataTag.DISTANCE -> pojo.distance = JsonUtils.readFloat(reader);
                    case _DistanceDamageDataTag.DAMAGE -> pojo.damage = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _DistanceDamageData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _DistanceDamageDataTag.DISTANCE, this.distance);
            JsonUtils.writeFloat(writer, _DistanceDamageDataTag.DAMAGE, this.damage);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getDistance() {
        return distance;
    }
    public float getDamage() {
        return damage;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
    public void setDamage(float damage) {
        this.damage = damage;
    }
}