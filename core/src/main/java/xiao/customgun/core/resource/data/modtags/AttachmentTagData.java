/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.modtags;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

/**
 * 存放配件ResourceLocation的Tag
 */
public class AttachmentTagData extends _SimpleTagData<AttachmentTagData> {

    private static final AttachmentTagData PARSER = new AttachmentTagData();
    public static AttachmentTagData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected AttachmentTagData fromJsonReader(JsonReader reader) throws IOException {
        AttachmentTagData pojo = new AttachmentTagData();
        pojo.setTags(JsonUtils.readArraySet(reader, JsonUtils::readString));
        return pojo;
    }

    public static void toJson(JsonWriter writer, AttachmentTagData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
        else writer.nullValue();
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginArray(); {
            for (String tag : this.getTags()) {
                if (tag != null) writer.value(tag);
            }
        }
        writer.endArray();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }
}