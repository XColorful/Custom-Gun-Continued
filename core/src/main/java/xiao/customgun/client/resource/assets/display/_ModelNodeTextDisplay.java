/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public class _ModelNodeTextDisplay extends ResourcePojo<_ModelNodeTextDisplay> {

    private static final _ModelNodeTextDisplay PARSER = new _ModelNodeTextDisplay();
    public static _ModelNodeTextDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ModelNodeTextDisplay fromJsonReader(JsonReader reader) throws IOException {
        _ModelNodeTextDisplay pojo = new _ModelNodeTextDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                reader.nextName();
                reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ModelNodeTextDisplay pojo) throws IOException {
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