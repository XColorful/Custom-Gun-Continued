/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.animation.bedrock.animation._SoundEffectsTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Map;

public final class _SoundEffects extends ResourcePojo<_SoundEffects> {

    private final Double2ObjectRBTreeMap<Identifier> keyframes = new Double2ObjectRBTreeMap<>();

    private static final _SoundEffects PARSER = new _SoundEffects();
    public static _SoundEffects fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _SoundEffects fromJsonReader(JsonReader reader) throws IOException {
        _SoundEffects pojo = new _SoundEffects();
        JsonToken peek = reader.peek();

        if (peek == JsonToken.NULL) {
            reader.nextNull();
            return pojo;
        }

        // 解析 {"0.0": {"effect": "tacz:xxx"}, "0.5": {"effect": "tacz:yyy"}}
        if (peek == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String timeStr = reader.nextName();
                try {
                    double time = Double.parseDouble(timeStr);

                    // 剥离内部的 {"effect": "..."} 对象
                    Identifier loc = null;
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String key = reader.nextName();
                            if (_SoundEffectsTag.EFFECT.equals(key) && reader.peek() == JsonToken.STRING) {
                                loc = JsonUtils.mcRegistry.createResourceLocation(reader.nextString());
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }

                    if (loc != null) {
                        pojo.keyframes.put(time, loc);
                    }
                } catch (NumberFormatException e) {
                    reader.skipValue(); // 容错处理
                }
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }

        return pojo;
    }

    public static void toJson(JsonWriter writer, _SoundEffects pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        if (this.keyframes.isEmpty()) {
            writer.nullValue();
            return;
        }
        writer.beginObject();
        for (Map.Entry<Double, Identifier> entry : this.keyframes.double2ObjectEntrySet()) {
            writer.name(String.valueOf(entry.getKey()));
            writer.beginObject(); {
                writer.name(_SoundEffectsTag.EFFECT).value(entry.getValue().toString());
            }
            writer.endObject();
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Double2ObjectRBTreeMap<Identifier> getKeyframes() {
        return this.keyframes;
    }
}