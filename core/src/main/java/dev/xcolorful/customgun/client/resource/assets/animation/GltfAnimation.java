/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.animation;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public final class GltfAnimation extends ResourcePojo<GltfAnimation> {

    private static final GltfAnimation PARSER = new GltfAnimation();
    public static GltfAnimation fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GltfAnimation fromJsonReader(JsonReader reader) throws IOException {
        GltfAnimation pojo = new GltfAnimation();
        reader.beginObject(); {
            while (reader.hasNext()) {
                reader.nextName();
                reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, GltfAnimation pojo) throws IOException {
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