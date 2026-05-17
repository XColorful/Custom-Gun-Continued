/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.gun.FireSoundType;
import xiao.customgun.core.api.resource.data.data.gun._FireSoundDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _FireSoundData extends ResourcePojo<_FireSoundData> {

    /**
     * {@link FireSoundType}
     */
    private float normalMultiplier = 1.0F;
    private float silencedMultiplier = 1.0F;

    private static final _FireSoundData PARSER = new _FireSoundData();
    public static _FireSoundData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _FireSoundData fromJsonReader(JsonReader reader) throws IOException {
        _FireSoundData pojo = new _FireSoundData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FireSoundDataTag.NORMAL_MULTIPLIER, _FireSoundDataTag.NORMAL_MULTIPLIER_OLD1 -> pojo.normalMultiplier = JsonUtils.readFloat(reader);
                    case _FireSoundDataTag.SILENCED_MULTIPLIER, _FireSoundDataTag.SILENCED_MULTIPLIER_OLD1 -> pojo.silencedMultiplier = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FireSoundData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _FireSoundDataTag.NORMAL_MULTIPLIER, this.normalMultiplier);
            JsonUtils.writeFloat(writer, _FireSoundDataTag.SILENCED_MULTIPLIER, this.silencedMultiplier);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getNormalMultiplier() {
        return normalMultiplier;
    }
    public float getSilencedMultiplier() {
        return silencedMultiplier;
    }

    public void setNormalMultiplier(float normalMultiplier) {
        this.normalMultiplier = normalMultiplier;
    }
    public void setSilencedMultiplier(float silencedMultiplier) {
        this.silencedMultiplier = silencedMultiplier;
    }
}