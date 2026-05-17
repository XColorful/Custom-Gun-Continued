/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.ammo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public class _ShellDisplay extends ResourcePojo<_ShellDisplay> {

    private static final _ShellDisplay PARSER = new _ShellDisplay();
    public static _ShellDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ShellDisplay fromJsonReader(JsonReader reader) throws IOException {
        _ShellDisplay pojo = new _ShellDisplay();
        reader.beginObject(); {
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ShellDisplay pojo) throws IOException {
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