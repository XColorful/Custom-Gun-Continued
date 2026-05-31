/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.assets.display._ModelTransformScaleTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _ModelTransformScale extends ResourcePojo<_ModelTransformScale> {

    private float[] thirdPersonScale;
    private float[] groundScale;
    private float[] fixedScale;

    private static final _ModelTransformScale PARSER = new _ModelTransformScale();
    public static _ModelTransformScale fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ModelTransformScale fromJsonReader(JsonReader reader) throws IOException {
        _ModelTransformScale pojo = new _ModelTransformScale();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ModelTransformScaleTag.THIRD_PERSON_SCALE, _ModelTransformScaleTag.THIRD_PERSON_SCALE_OLD1 -> pojo.thirdPersonScale = JsonUtils.readFloatArrayFast(reader, 3);
                    case _ModelTransformScaleTag.GROUND_SCALE, _ModelTransformScaleTag.GROUND_SCALE_OLD1 -> pojo.groundScale = JsonUtils.readFloatArrayFast(reader, 3);
                    case _ModelTransformScaleTag.FIXED_SCALE, _ModelTransformScaleTag.FIXED_SCALE_OLD1 -> pojo.fixedScale = JsonUtils.readFloatArrayFast(reader, 3);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ModelTransformScale pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloatArray(writer, _ModelTransformScaleTag.THIRD_PERSON_SCALE, this.thirdPersonScale);
            JsonUtils.writeFloatArray(writer, _ModelTransformScaleTag.GROUND_SCALE, this.groundScale);
            JsonUtils.writeFloatArray(writer, _ModelTransformScaleTag.FIXED_SCALE, this.fixedScale);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        boolean n1 = (this.thirdPersonScale == null | this.groundScale == null | this.fixedScale == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float[] getThirdPersonScale() {
        return thirdPersonScale;
    }
    public float[] getGroundScale() {
        return groundScale;
    }
    public float[] getFixedScale() {
        return fixedScale;
    }

    public void setThirdPersonScale(float[] thirdPersonScale) {
        this.thirdPersonScale = thirdPersonScale;
    }
    public void setGroundScale(float[] groundScale) {
        this.groundScale = groundScale;
    }
    public void setFixedScale(float[] fixedScale) {
        this.fixedScale = fixedScale;
    }
}
