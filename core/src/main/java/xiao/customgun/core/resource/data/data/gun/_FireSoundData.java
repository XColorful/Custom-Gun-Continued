/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._FireSoundDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _FireSoundData extends ResourcePojo<_FireSoundData> {

    private float normal = 1.0F;
    private float silenced = 1.0F;

    private static final _FireSoundData PARSER = new _FireSoundData();
    public static _FireSoundData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _FireSoundData fromJsonReader(JsonReader reader) throws IOException {
        _FireSoundData pojo = new _FireSoundData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FireSoundDataTag.NORMAL -> pojo.normal = JsonUtils.readFloat(reader);
                    case _FireSoundDataTag.SILENCED -> pojo.silenced = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FireSoundData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _FireSoundDataTag.NORMAL, this.normal);
            JsonUtils.writeFloat(writer, _FireSoundDataTag.SILENCED, this.silenced);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getNormal() {
        return normal;
    }
    public float getSilenced() {
        return silenced;
    }

    public void setNormal(float normal) {
        this.normal = normal;
    }
    public void setSilenced(float silenced) {
        this.silenced = silenced;
    }
}