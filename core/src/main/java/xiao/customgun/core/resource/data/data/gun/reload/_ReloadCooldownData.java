/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun.reload;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun.reload._ReloadCooldownDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _ReloadCooldownData extends ResourcePojo<_ReloadCooldownData> {

    private float empty = 2.5F;
    private float tactical = 2.0F;

    private static final _ReloadCooldownData PARSER = new _ReloadCooldownData();
    public static _ReloadCooldownData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ReloadCooldownData fromJsonReader(JsonReader reader) throws IOException {
        _ReloadCooldownData pojo = new _ReloadCooldownData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ReloadCooldownDataTag.EMPTY -> pojo.empty = JsonUtils.readFloat(reader);
                    case _ReloadCooldownDataTag.TACTICAL -> pojo.tactical = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ReloadCooldownData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _ReloadCooldownDataTag.EMPTY, this.empty);
            JsonUtils.writeFloat(writer, _ReloadCooldownDataTag.TACTICAL, this.tactical);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getEmpty() {
        return empty;
    }
    public float getTactical() {
        return tactical;
    }

    public void setEmpty(float empty) {
        this.empty = empty;
    }
    public void setTactical(float tactical) {
        this.tactical = tactical;
    }
}