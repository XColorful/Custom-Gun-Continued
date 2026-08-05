/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone._Cube;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry._BoneTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public final class _Bone extends ResourcePojo<_Bone> {

    private String name;
    private String parent;
    private float[] pivot;
    private float[] rotation;
    private List<_Cube> cubes;
    private boolean mirror = false;

    private static final _Bone PARSER = new _Bone();
    public static _Bone fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _Bone fromJsonReader(JsonReader reader) throws IOException {
        _Bone pojo = new _Bone();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BoneTag.NAME -> pojo.name = JsonUtils.readString(reader);
                    case _BoneTag.PARENT -> pojo.parent = JsonUtils.readString(reader);
                    case _BoneTag.PIVOT -> pojo.pivot = JsonUtils.readFloatArrayFast(reader, 3);
                    case _BoneTag.ROTATION -> pojo.rotation = JsonUtils.readFloatArrayFast(reader, 3);
                    case _BoneTag.CUBES -> pojo.cubes = JsonUtils.readList(reader, _Cube::fromJson);
                    case _BoneTag.MIRROR -> pojo.mirror = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _Bone pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, _BoneTag.NAME, this.name);
            JsonUtils.writeString(writer, _BoneTag.PARENT, this.parent);
            JsonUtils.writeFloatArray(writer, _BoneTag.PIVOT, this.pivot);
            JsonUtils.writeFloatArray(writer, _BoneTag.ROTATION, this.rotation);
            JsonUtils.writeList(writer, _BoneTag.CUBES, this.cubes, _Cube::toJson);
            if (this.mirror) JsonUtils.writeBoolean(writer, _BoneTag.MIRROR, this.mirror);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getName() {
        return name;
    }
    public String getParent() {
        return parent;
    }
    public float[] getPivot() {
        return pivot;
    }
    public float[] getRotation() {
        return rotation;
    }
    public List<_Cube> getCubes() {
        return cubes;
    }
    public boolean getMirror() {
        return mirror;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setParent(String parent) {
        this.parent = parent;
    }
    public void setPivot(float[] pivot) {
        this.pivot = pivot;
    }
    public void setRotation(float[] rotation) {
        this.rotation = rotation;
    }
    public void setCubes(List<_Cube> cubes) {
        this.cubes = cubes;
    }
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }
}