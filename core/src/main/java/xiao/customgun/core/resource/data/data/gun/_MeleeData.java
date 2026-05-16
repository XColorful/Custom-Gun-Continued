/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._MeleeDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _MeleeData extends ResourcePojo<_MeleeData> {

    private float meleeDistance = 1.0F;
    private float meleeCooldown = 1.0F;
    private _DefaultMeleeData defaultMeleeData;

    private static final _MeleeData PARSER = new _MeleeData();
    public static _MeleeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _MeleeData fromJsonReader(JsonReader reader) throws IOException {
        _MeleeData pojo = new _MeleeData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _MeleeDataTag.MELEE_DISTANCE, _MeleeDataTag.MELEE_DISTANCE_OLD1 -> pojo.meleeDistance = JsonUtils.readFloat(reader);
                    case _MeleeDataTag.MELEE_COOLDOWN, _MeleeDataTag.MELEE_COOLDOWN_OLD1 -> pojo.meleeCooldown = JsonUtils.readFloat(reader);
                    case _MeleeDataTag.DEFAULT_MELEE_DATA, _MeleeDataTag.DEFAULT_MELEE_DATA_OLD1 -> pojo.defaultMeleeData = JsonUtils.read(reader, _DefaultMeleeData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MeleeData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _MeleeDataTag.MELEE_DISTANCE, this.meleeDistance);
            JsonUtils.writeFloat(writer, _MeleeDataTag.MELEE_COOLDOWN, this.meleeCooldown);
            JsonUtils.write(writer, _MeleeDataTag.DEFAULT_MELEE_DATA, this.defaultMeleeData, _DefaultMeleeData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getMeleeDistance() {
        return meleeDistance;
    }
    public float getMeleeCooldown() {
        return meleeCooldown;
    }
    public _DefaultMeleeData getDefaultMeleeData() {
        return defaultMeleeData;
    }

    public void setMeleeDistance(float meleeDistance) {
        this.meleeDistance = meleeDistance;
    }
    public void setMeleeCooldown(float meleeCooldown) {
        this.meleeCooldown = meleeCooldown;
    }
    public void setDefaultMeleeData(_DefaultMeleeData defaultMeleeData) {
        this.defaultMeleeData = defaultMeleeData;
    }
}