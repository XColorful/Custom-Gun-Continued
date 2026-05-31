/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.assets.display._ModelTransformTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ModelTransform extends ResourcePojo<_ModelTransform> {

    private @Nullable _ModelTransformScale scale;

    private static final _ModelTransform PARSER = new _ModelTransform();
    public static _ModelTransform fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ModelTransform fromJsonReader(JsonReader reader) throws IOException {
        _ModelTransform pojo = new _ModelTransform();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ModelTransformTag.SCALE -> pojo.scale = JsonUtils.read(reader, _ModelTransformScale::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ModelTransform pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, _ModelTransformTag.SCALE, this.scale, _ModelTransformScale::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (this.scale != null) this.scale.validate();
        boolean v1 = (this.scale == null || this.scale.isValid());
        if (!v1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public @Nullable  _ModelTransformScale getScale() {
        return scale;
    }

    public void setScale(_ModelTransformScale scale) {
        this.scale = scale;
    }
}