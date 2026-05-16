/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.gun.FireSoundType;
import xiao.customgun.core.api.resource.data.data.attachment._MuzzleModifierDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _MuzzleModifierData extends ResourcePojo<_MuzzleModifierData> {

    private FireSoundType fireSoundType;

    private static final _MuzzleModifierData PARSER = new _MuzzleModifierData();
    public static _MuzzleModifierData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _MuzzleModifierData fromJsonReader(JsonReader reader) throws IOException {
        _MuzzleModifierData pojo = new _MuzzleModifierData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _MuzzleModifierDataTag.FIRE_SOUND_TYPE -> pojo.fireSoundType = JsonUtils.readFromString(reader, FireSoundType::fromString); case _MuzzleModifierDataTag.FIRE_SOUND_TYPE_OLD1 -> pojo.fireSoundType = JsonUtils.readBoolean(reader) ? FireSoundType.SILENCED : FireSoundType.NORMAL;
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MuzzleModifierData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeToString(writer, _MuzzleModifierDataTag.FIRE_SOUND_TYPE, this.fireSoundType);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public FireSoundType getFireSoundType() {
        return fireSoundType;
    }

    public void setFireSoundType(FireSoundType fireSoundType) {
        this.fireSoundType = fireSoundType;
    }
}