/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.model.bedrock.geometry.bone;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.client.resource.assets.model.bedrock.geometry.bone.cube._Uv;
import xiao.customgun.core.api.resource.assets.model.bedrock.geometry.bone._CubeTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _Cube extends ResourcePojo<_Cube> {

    private float[] origin;
    private float[] size;
    private float inflate;
    private float[] pivot;
    private float[] rotation;
    private _Uv uv;

    private static final _Cube PARSER = new _Cube();
    public static _Cube fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _Cube fromJsonReader(JsonReader reader) throws IOException {
        _Cube pojo = new _Cube();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _CubeTag.ORIGIN -> pojo.origin = JsonUtils.readFloatArrayFast(reader, 3);
                    case _CubeTag.SIZE -> pojo.size = JsonUtils.readFloatArrayFast(reader, 3);
                    case _CubeTag.INFLATE -> pojo.inflate = JsonUtils.readFloat(reader);
                    case _CubeTag.PIVOT -> pojo.pivot = JsonUtils.readFloatArrayFast(reader, 3);
                    case _CubeTag.ROTATION -> pojo.rotation = JsonUtils.readFloatArrayFast(reader, 3);
                    case _CubeTag.UV -> pojo.uv = _Uv.fromJson(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _Cube pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloatArray(writer, _CubeTag.ORIGIN, this.origin);
            JsonUtils.writeFloatArray(writer, _CubeTag.SIZE, this.size);
            JsonUtils.writeFloat(writer, _CubeTag.INFLATE, this.inflate);
            JsonUtils.writeFloatArray(writer, _CubeTag.PIVOT, this.pivot);
            JsonUtils.writeFloatArray(writer, _CubeTag.ROTATION, this.rotation);
            JsonUtils.write(writer, _CubeTag.UV, this.uv, _Uv::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float[] getOrigin() {
        return origin;
    }
    public float[] getSize() {
        return size;
    }
    public float getInflate() {
        return inflate;
    }
    public float[] getPivot() {
        return pivot;
    }
    public float[] getRotation() {
        return rotation;
    }
    public _Uv getUv() {
        return uv;
    }

    public void setOrigin(float[] origin) {
        this.origin = origin;
    }
    public void setSize(float[] size) {
        this.size = size;
    }
    public void setInflate(float inflate) {
        this.inflate = inflate;
    }
    public void setPivot(float[] pivot) {
        this.pivot = pivot;
    }
    public void setRotation(float[] rotation) {
        this.rotation = rotation;
    }
    public void setUv(_Uv uv) {
        this.uv = uv;
    }
}