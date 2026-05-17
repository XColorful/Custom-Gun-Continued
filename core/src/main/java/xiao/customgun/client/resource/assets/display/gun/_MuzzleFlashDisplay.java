/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public final class _MuzzleFlashDisplay extends ResourcePojo<_MuzzleFlashDisplay> {

    private static final _MuzzleFlashDisplay PARSER = new _MuzzleFlashDisplay();
    public static _MuzzleFlashDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _MuzzleFlashDisplay fromJsonReader(JsonReader reader) throws IOException {
        _MuzzleFlashDisplay pojo = new _MuzzleFlashDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                reader.nextName();
                reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MuzzleFlashDisplay pojo) throws IOException {
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

    // --------Getter & Setter--------
}