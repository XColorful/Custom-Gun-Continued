/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._MovementDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

/**
 * TODO 重命名该类，既有 speed 又有别的参数
 */
public final class _MovementData extends ResourcePojo<_MovementData> {

    private float base = 0F;
    private float aim = 0F;
    private float reload = 0F;

    private static final _MovementData PARSER = new _MovementData();
    public static _MovementData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _MovementData fromJsonReader(JsonReader reader) throws IOException {
        _MovementData pojo = new _MovementData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _MovementDataTag.BASE -> pojo.base = JsonUtils.readFloat(reader);
                    case _MovementDataTag.AIM -> pojo.aim = JsonUtils.readFloat(reader);
                    case _MovementDataTag.RELOAD -> pojo.reload = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MovementData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _MovementDataTag.BASE, this.base);
            JsonUtils.writeFloat(writer, _MovementDataTag.AIM, this.aim);
            JsonUtils.writeFloat(writer, _MovementDataTag.RELOAD, this.reload);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getBase() {
        return base;
    }
    public float getAim() {
        return aim;
    }
    public float getReload() {
        return reload;
    }

    public void setBase(float base) {
        this.base = base;
    }
    public void setAim(float aim) {
        this.aim = aim;
    }
    public void setReload(float reload) {
        this.reload = reload;
    }
}