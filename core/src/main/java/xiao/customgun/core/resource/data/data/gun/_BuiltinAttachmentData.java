/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public class _BuiltinAttachmentData extends ResourcePojo<_BuiltinAttachmentData> {

    private static final _BuiltinAttachmentData PARSER = new _BuiltinAttachmentData();
    public static _BuiltinAttachmentData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _BuiltinAttachmentData fromJsonReader(JsonReader reader) throws IOException {
        _BuiltinAttachmentData pojo = new _BuiltinAttachmentData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }
    public static void toJson(JsonWriter writer, _BuiltinAttachmentData pojo) throws IOException {
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