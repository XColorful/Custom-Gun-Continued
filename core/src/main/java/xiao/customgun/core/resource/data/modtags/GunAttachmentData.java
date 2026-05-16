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
 * 枪械的可安装的配件数据
 * 指定单个配件或选中 {@link AttachmentTagData}
 */
public class GunAttachmentData extends _SimpleTagData<GunAttachmentData> {

    private static final GunAttachmentData PARSER = new GunAttachmentData();
    public static GunAttachmentData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GunAttachmentData fromJsonReader(JsonReader reader) throws IOException {
        GunAttachmentData pojo = new GunAttachmentData();
        pojo.setTags(JsonUtils.readArraySet(reader, JsonUtils::readString));
        return pojo;
    }

    public static void toJson(JsonWriter writer, GunAttachmentData pojo) throws IOException {
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