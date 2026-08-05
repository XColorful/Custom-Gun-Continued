/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.gun.recoil;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun.recoil._RecoilEntryDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _RecoilEntryData extends ResourcePojo<_RecoilEntryData> {

    private float time = 0;
    private float[] range;

    private static final _RecoilEntryData PARSER = new _RecoilEntryData();
    public static _RecoilEntryData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _RecoilEntryData fromJsonReader(JsonReader reader) throws IOException {
        _RecoilEntryData pojo = new _RecoilEntryData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _RecoilEntryDataTag.TIME -> pojo.time = JsonUtils.readFloat(reader);
                    case _RecoilEntryDataTag.RANGE -> pojo.range = JsonUtils.readFloatArrayFast(reader, 2);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _RecoilEntryData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _RecoilEntryDataTag.TIME, this.time);
            JsonUtils.writeFloatArray(writer, _RecoilEntryDataTag.RANGE, this.range);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        boolean n1 = (this.range == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getTime() {
        return time;
    }
    public float[] getRange() {
        return range;
    }

    public void setTime(float time) {
        this.time = time;
    }
    public void setRange(float[] range) {
        this.range = range;
    }

    // --------Back compatibility--------
}