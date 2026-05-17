/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.animation.bedrock.animation;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import xiao.customgun.client.resource.assets.animation.bedrock.animation.bone._KeyFrame;
import xiao.customgun.client.util.ClientJsonUtils;
import xiao.customgun.core.api.resource.assets.animation.bedrock.animation._BoneTag;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public final class _Bone extends ResourcePojo<_Bone> {

    private Double2ObjectRBTreeMap<_KeyFrame> rotation;
    private Double2ObjectRBTreeMap<_KeyFrame> position;
    private Double2ObjectRBTreeMap<_KeyFrame> scale;

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
                    case _BoneTag.ROTATION -> pojo.rotation = ClientJsonUtils.readKeyFrames(reader);
                    case _BoneTag.POSITION -> pojo.position = ClientJsonUtils.readKeyFrames(reader);
                    case _BoneTag.SCALE -> pojo.scale = ClientJsonUtils.readKeyFrames(reader);
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
            ClientJsonUtils.writeKeyFrames(writer, _BoneTag.ROTATION, this.rotation);
            ClientJsonUtils.writeKeyFrames(writer, _BoneTag.POSITION, this.position);
            ClientJsonUtils.writeKeyFrames(writer, _BoneTag.SCALE, this.scale);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Double2ObjectRBTreeMap<_KeyFrame> getRotation() {
        return rotation;
    }
    public Double2ObjectRBTreeMap<_KeyFrame> getPosition() {
        return position;
    }
    public Double2ObjectRBTreeMap<_KeyFrame> getScale() {
        return scale;
    }

    public void setRotation(Double2ObjectRBTreeMap<_KeyFrame> rotation) {
        this.rotation = rotation;
    }
    public void setPosition(Double2ObjectRBTreeMap<_KeyFrame> position) {
        this.position = position;
    }
    public void setScale(Double2ObjectRBTreeMap<_KeyFrame> scale) {
        this.scale = scale;
    }
}