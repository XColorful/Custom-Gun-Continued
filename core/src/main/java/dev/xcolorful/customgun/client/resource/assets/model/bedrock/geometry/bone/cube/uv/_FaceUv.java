/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube.uv;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.bone.cube.uv._FaceUvTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _FaceUv extends ResourcePojo<_FaceUv> {

    private float[] uv;
    private float[] uvSize;

    private static final _FaceUv PARSER = new _FaceUv();
    public static _FaceUv fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _FaceUv fromJsonReader(JsonReader reader) throws IOException {
        _FaceUv pojo = new _FaceUv();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FaceUvTag.UV -> pojo.uv = JsonUtils.readFloatArrayFast(reader, 2);
                    case _FaceUvTag.UV_SIZE -> pojo.uvSize = JsonUtils.readFloatArrayFast(reader,  2);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FaceUv pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloatArray(writer, _FaceUvTag.UV, this.uv);
            JsonUtils.writeFloatArray(writer, _FaceUvTag.UV_SIZE, this.uvSize);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float[] getUv() {
        return uv;
    }
    public float[] getUvSize() {
        return uvSize;
    }

    public void setUv(float[] uv) {
        this.uv = uv;
    }
    public void setUvSize(float[] uvSize) {
        this.uvSize = uvSize;
    }

    // --------Special--------
    public static final _FaceUv EMPTY = empty();
    public static _FaceUv empty() {
        _FaceUv face = new _FaceUv();
        face.uv = new float[]{0, 0};
        face.uvSize = new float[]{0, 0};
        return face;
    }
    public static _FaceUv single16X() {
        _FaceUv face = new _FaceUv();
        face.uv = new float[]{0, 0};
        face.uvSize = new float[]{16, 16};
        return face;
    }
}