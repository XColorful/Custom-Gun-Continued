/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun.bullet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public class _BulletSkillData extends ResourcePojo<_BulletSkillData> {

    private static final _BulletSkillData PARSER = new _BulletSkillData();
    public static _BulletSkillData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _BulletSkillData fromJsonReader(JsonReader reader) throws IOException {
        _BulletSkillData pojo = new _BulletSkillData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _BulletSkillData pojo) throws IOException {
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
}