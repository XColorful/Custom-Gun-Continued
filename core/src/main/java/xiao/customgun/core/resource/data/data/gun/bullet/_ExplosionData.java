/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun.bullet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public class _ExplosionData extends ResourcePojo<_ExplosionData> {

    private static final _ExplosionData PARSER = new _ExplosionData();
    public static _ExplosionData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ExplosionData fromJsonReader(JsonReader reader) throws IOException {
        _ExplosionData pojo = new _ExplosionData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ExplosionData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }
}