/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation.bone;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.api.animation.interpolator.LerpMode;
import dev.xcolorful.customgun.client.util.ClientJsonUtils;
import dev.xcolorful.customgun.core.api.resource.assets.animation.bedrock.animation.bone._KeyFrameTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class _KeyFrame extends ResourcePojo<_KeyFrame> {

    private float[] pre;
    private float[] post;
    private float[] data;
    private @Nullable LerpMode lerpMode;

    private static final _KeyFrame PARSER = new _KeyFrame();
    public static _KeyFrame fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _KeyFrame fromJsonReader(JsonReader reader) throws IOException {
        _KeyFrame pojo = new _KeyFrame();
        JsonToken token = reader.peek();

        if (token == JsonToken.BEGIN_ARRAY) {
            // 形态 1 & 2: 对应简写数组 [x, y, z]，直接视为核心 data 数据
            pojo.data = ClientJsonUtils.readFloatArrayWithMolangTolerance(reader, 3);
        } else if (token == JsonToken.BEGIN_OBJECT) {
            // 形态 3: 对应带有 post, pre, data, lerp_mode 的复杂 JsonObject
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _KeyFrameTag.PRE -> pojo.pre = ClientJsonUtils.readFloatArrayWithMolangTolerance(reader, 3);
                    case _KeyFrameTag.POST -> pojo.post = ClientJsonUtils.readFloatArrayWithMolangTolerance(reader, 3);
                    case _KeyFrameTag.DATA -> pojo.data = ClientJsonUtils.readFloatArrayWithMolangTolerance(reader, 3);
                    case _KeyFrameTag.LERP_MODE -> pojo.lerpMode = JsonUtils.readFromString(reader, LerpMode::fromString);
                    default -> reader.skipValue();
                }
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }
        return pojo;
    }

    public static void toJson(JsonWriter writer, _KeyFrame pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        // 如果只是纯 data 数据，直接写简化的纯数组
        if (this.pre == null && this.post == null && this.lerpMode == null && this.data != null) {
            writer.beginArray(); {
                for (float f : this.data) {
                    writer.value(f);
                }
            }
            writer.endArray();
            return;
        }
        writer.beginObject(); {
            JsonUtils.writeFloatArray(writer, _KeyFrameTag.PRE, this.pre);
            JsonUtils.writeFloatArray(writer, _KeyFrameTag.POST, this.post);
            JsonUtils.writeFloatArray(writer, _KeyFrameTag.DATA, this.data);
            JsonUtils.writeToString(writer, _KeyFrameTag.LERP_MODE, this.lerpMode);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float[] getPre() {
        return pre;
    }
    public float[] getPost() {
        return post;
    }
    public float[] getData() {
        return data;
    }
    public @Nullable LerpMode getLerpMode() {
        return lerpMode;
    }

    public void setPre(float[] pre) {
        this.pre = pre;
    }
    public void setPost(float[] post) {
        this.post = post;
    }
    public void setData(float[] data) {
        this.data = data;
    }
    public void setLerpMode(LerpMode lerpMode) {
        this.lerpMode = lerpMode;
    }

    // --------Special--------

    // 用于单帧数组直接构造静态关键帧
    public static _KeyFrame ofStatic(float[] data) {
        _KeyFrame kf = new _KeyFrame();
        kf.data = data;
        return kf;
    }
}