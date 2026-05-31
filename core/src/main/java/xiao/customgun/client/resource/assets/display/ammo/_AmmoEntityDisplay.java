/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.ammo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.client.resource.assets.display._AssetsDisplay;
import xiao.customgun.core.api.resource.assets.display.ammo._AmmoEntityDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _AmmoEntityDisplay extends _AssetsDisplay<_AmmoEntityDisplay> {

    private static final _AmmoEntityDisplay PARSER = new _AmmoEntityDisplay();
    public static _AmmoEntityDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _AmmoEntityDisplay fromJsonReader(JsonReader reader) throws IOException {
        _AmmoEntityDisplay pojo = new _AmmoEntityDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _AmmoEntityDisplayTag.MODEL_LOCATION, _AmmoEntityDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case _AmmoEntityDisplayTag.TEXTURE_LOCATION, _AmmoEntityDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _AmmoEntityDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _AmmoEntityDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, _AmmoEntityDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
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
}