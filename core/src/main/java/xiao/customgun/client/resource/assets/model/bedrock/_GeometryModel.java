/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.model.bedrock;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import xiao.customgun.client.resource.assets.model.bedrock.geometry._Description;
import xiao.customgun.core.api.resource.assets.model.bedrock._GeometryModelTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public class _GeometryModel extends ResourcePojo<_GeometryModel> {

    private _Description description;
    private List<_Bone> bones;

    private static final _GeometryModel PARSER = new _GeometryModel();
    public static _GeometryModel fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _GeometryModel fromJsonReader(JsonReader reader) throws IOException {
        _GeometryModel pojo = new _GeometryModel();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _GeometryModelTag.DESCRIPTION -> pojo.description = JsonUtils.read(reader, _Description::fromJson);
                    case _GeometryModelTag.BONES -> pojo.bones = JsonUtils.readList(reader, _Bone::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _GeometryModel pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, _GeometryModelTag.DESCRIPTION, this.description, _Description::toJson);
            JsonUtils.writeList(writer, _GeometryModelTag.BONES, this.bones, _Bone::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public _Description getDescription() {
        return description;
    }
    public List<_Bone> getBones() {
        return bones;
    }

    public void setDescription(_Description description) {
        this.description = description;
    }
    public void setBones(List<_Bone> bones) {
        this.bones = bones;
    }
}