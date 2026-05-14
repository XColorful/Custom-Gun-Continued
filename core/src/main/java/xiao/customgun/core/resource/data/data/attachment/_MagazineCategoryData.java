/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.attachment.MagazineCategory;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _MagazineCategoryData extends ResourcePojo<_MagazineCategoryData> {

    private MagazineCategory data;
    private int dataRaw = 0;

    private static final _MagazineCategoryData PARSER = new _MagazineCategoryData();
    public static _MagazineCategoryData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _MagazineCategoryData fromJsonReader(JsonReader reader) throws IOException {
        _MagazineCategoryData pojo = new _MagazineCategoryData();
        pojo.dataRaw = JsonUtils.readInt(reader);
        pojo.data = MagazineCategory.fromIndex(pojo.dataRaw);
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MagazineCategoryData data) throws IOException {
        if (data != null) data.toJson(writer);
        else writer.nullValue();
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        if (this.dataRaw != 0) writer.value(this.dataRaw);
        else if (this.data != null) writer.value(this.data.getIndex());
        else writer.nullValue();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public MagazineCategory get() {
        return data;
    }
    public int getRaw() {
        return dataRaw;
    }

    public void set(MagazineCategory value) {
        this.data = value;
        if (value != null) this.dataRaw = value.getIndex();
        else this.dataRaw = 0;
    }
    public void setRaw(int rawValue) {
        this.dataRaw = rawValue;
        this.data = MagazineCategory.fromIndex(rawValue);
    }
}