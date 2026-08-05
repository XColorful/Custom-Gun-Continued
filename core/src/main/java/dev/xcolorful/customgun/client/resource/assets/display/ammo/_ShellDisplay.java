/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.display.ammo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.display._AssetsDisplay;
import dev.xcolorful.customgun.core.api.resource.assets.display.ammo._ShellDisplayTag;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ShellDisplay extends _AssetsDisplay<_ShellDisplay> {

    private static final _ShellDisplay PARSER = new _ShellDisplay();
    public static _ShellDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ShellDisplay fromJsonReader(JsonReader reader) throws IOException {
        _ShellDisplay pojo = new _ShellDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ShellDisplayTag.MODEL_LOCATION, _ShellDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case _ShellDisplayTag.TEXTURE_LOCATION, _ShellDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    default -> reader.skipValue();
                }
            }
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
            JsonUtils.writeResourceLocation(writer, _ShellDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, _ShellDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        super.validatePojo();
        if (!this.isValid()) return;

        this.setValid(true);
    }

    // --------Getter & Setter--------

    // --------Back compatibility--------

    @Override
    public _ShellDisplay applyBackCompatibility() {
        super.applyBackCompatibility();
        return this;
    }
}