/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun._MeleeDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _MeleeData extends ResourcePojo<_MeleeData> {

    private float gunBaseLength = 1.0F;
    private _DefaultMeleeData defaultMeleeData;

    private static final _MeleeData PARSER = new _MeleeData();
    public static _MeleeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _MeleeData fromJsonReader(JsonReader reader) throws IOException {
        _MeleeData pojo = new _MeleeData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _MeleeDataTag.GUN_BASE_LENGTH, _MeleeDataTag.GUN_BASE_LENGTH_OLD1 -> pojo.gunBaseLength = JsonUtils.readFloat(reader);
                    case _MeleeDataTag.DEFAULT_MELEE_DATA, _MeleeDataTag.DEFAULT_MELEE_DATA_OLD1 -> pojo.defaultMeleeData = JsonUtils.read(reader, _DefaultMeleeData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MeleeData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _MeleeDataTag.GUN_BASE_LENGTH, this.gunBaseLength);
            JsonUtils.write(writer, _MeleeDataTag.DEFAULT_MELEE_DATA, this.defaultMeleeData, _DefaultMeleeData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.defaultMeleeData == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        this.defaultMeleeData.validate();
        boolean v1 = (this.defaultMeleeData.isValid());
        if (!(v1)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getGunBaseLength() {
        return gunBaseLength;
    }
    public _DefaultMeleeData getDefaultMeleeData() {
        return defaultMeleeData;
    }

    public void setGunBaseLength(float gunBaseLength) {
        this.gunBaseLength = gunBaseLength;
    }
    public void setDefaultMeleeData(_DefaultMeleeData defaultMeleeData) {
        this.defaultMeleeData = defaultMeleeData;
    }

    // --------Back compatibility--------

    @Override
    public _MeleeData applyBackCompatibility() {
        this.defaultMeleeData = this.defaultMeleeData == null ? new _DefaultMeleeData().applyBackCompatibility() : this.defaultMeleeData.applyBackCompatibility();
        return this;
    }
}