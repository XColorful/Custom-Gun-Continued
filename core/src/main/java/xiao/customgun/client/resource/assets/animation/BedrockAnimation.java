/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.animation;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.assets.animation.BedrockAnimationTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class BedrockAnimation extends ResourcePojo<BedrockAnimation> {

    private String version;

    private static final BedrockAnimation PARSER = new BedrockAnimation();
    public static BedrockAnimation fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected BedrockAnimation fromJsonReader(JsonReader reader) throws IOException {
        BedrockAnimation pojo = new BedrockAnimation();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case BedrockAnimationTag.VERSION -> pojo.version = JsonUtils.readString(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, BedrockAnimation pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, BedrockAnimationTag.VERSION, this.version);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}