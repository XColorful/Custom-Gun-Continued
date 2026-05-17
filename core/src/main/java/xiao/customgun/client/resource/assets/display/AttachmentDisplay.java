/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.assets.display.AttachmentDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class AttachmentDisplay extends _AssetsDisplay<AttachmentDisplay> {

    private static final AttachmentDisplay PARSER = new AttachmentDisplay();
    public static AttachmentDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AttachmentDisplay fromJsonReader(JsonReader reader) throws IOException {
        AttachmentDisplay pojo = new AttachmentDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AttachmentDisplayTag.MODEL_LOCATION, AttachmentDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case AttachmentDisplayTag.TEXTURE_LOCATION, AttachmentDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AttachmentDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, AttachmentDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, AttachmentDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
        }
        writer.endObject();
    }

    // --------Getter & Setter--------
}