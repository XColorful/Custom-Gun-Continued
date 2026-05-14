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
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.firemode._FireModeAdjustData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _FireModeData extends ResourcePojo<_FireModeData> {

    private _FireModeAdjustData auto;
    private _FireModeAdjustData semi;
    private _FireModeAdjustData burst;

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
                    case _FireModeDataTag.AUTO -> pojo.auto = JsonUtils.read(reader, _FireModeAdjustData::fromJson);
                    case _FireModeDataTag.SEMI -> pojo.semi = JsonUtils.read(reader, _FireModeAdjustData::fromJson);
                    case _FireModeDataTag.BURST -> pojo.burst = JsonUtils.read(reader, _FireModeAdjustData::fromJson);
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
            JsonUtils.write(writer, _FireModeDataTag.AUTO, this.auto, _FireModeAdjustData::toJson);
            JsonUtils.write(writer, _FireModeDataTag.SEMI, this.semi, _FireModeAdjustData::toJson);
            JsonUtils.write(writer, _FireModeDataTag.BURST, this.burst, _FireModeAdjustData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public _FireModeAdjustData getAuto() {
        return auto;
    }
    public _FireModeAdjustData getSemi() {
        return semi;
    }
    public _FireModeAdjustData getBurst() {
        return burst;
    }

    public void setAuto(_FireModeAdjustData auto) {
        this.auto = auto;
    }
    public void setSemi(_FireModeAdjustData semi) {
        this.semi = semi;
    }
    public void setBurst(_FireModeAdjustData burst) {
        this.burst = burst;
    }
}