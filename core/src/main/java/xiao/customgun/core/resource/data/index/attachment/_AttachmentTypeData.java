/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _AttachmentTypeData extends ResourcePojo<_AttachmentTypeData> {

    private AttachmentCategory data;
    private String dataRaw;

    private static final _AttachmentTypeData PARSER = new _AttachmentTypeData();
    public static _AttachmentTypeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _AttachmentTypeData fromJsonReader(JsonReader reader) throws IOException {
        _AttachmentTypeData pojo = new _AttachmentTypeData();
        pojo.dataRaw = JsonUtils.readString(reader);
        pojo.data = AttachmentCategory.fromString(pojo.dataRaw);
        return pojo;
    }

    public static void toJson(JsonWriter writer, _AttachmentTypeData data) throws IOException {
        if (data != null) data.toJson(writer);
        else writer.nullValue();
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        if (this.dataRaw != null) writer.value(this.dataRaw);
        else if (this.data != null) writer.value(this.data.getCategoryName());
        else writer.nullValue();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public AttachmentCategory get() {
        return data;
    }
    public String getRaw() {
        return dataRaw;
    }

    public void set(AttachmentCategory value) {
        this.data = value;
        if (value != null) this.dataRaw = value.getCategoryName();
        else this.dataRaw = null;
    }
    public void setRaw(String rawValue) {
        this.dataRaw = rawValue;
        this.data = AttachmentCategory.fromString(rawValue);
    }
}