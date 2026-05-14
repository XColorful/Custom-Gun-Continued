/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _FireModeTypeData extends ResourcePojo<_FireModeTypeData> {

    private FireModeType data;
    private String dataRaw;

    private static final _FireModeTypeData PARSER = new _FireModeTypeData();
    public static _FireModeTypeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _FireModeTypeData fromJsonReader(JsonReader reader) throws IOException {
        _FireModeTypeData pojo = new _FireModeTypeData();
        pojo.dataRaw = JsonUtils.readString(reader);
        pojo.data = FireModeType.fromString(pojo.dataRaw);
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FireModeTypeData data) throws IOException {
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

    public FireModeType get() {
        return data;
    }
    public String getRaw() {
        return dataRaw;
    }

    public void set(FireModeType value) {
        this.data = value;
        if (value != null) this.dataRaw = value.getCategoryName();
        else this.dataRaw = null;
    }
    public void setRaw(String rawValue) {
        this.dataRaw = rawValue;
        this.data = FireModeType.fromString(rawValue);
    }
}