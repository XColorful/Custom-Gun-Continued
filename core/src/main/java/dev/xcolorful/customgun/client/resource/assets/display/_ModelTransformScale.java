package dev.xcolorful.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.display._ModelTransformScaleTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

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
                    default -> this.readCustomKey(reader, key);
                }
            }
        }
        reader.endObject();
        return pojo;
    }
    @Override
    protected boolean readCustomKey(JsonReader reader, String key) throws IOException {
        return super.readCustomKey(reader, key);
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
            writeCustomValue(writer);
        }
        writer.endObject();
    }
    @Override
    protected void writeCustomValue(JsonWriter writer) {
        super.writeCustomValue(writer);
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.thirdPersonScale == null | this.groundScale == null | this.fixedScale == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected _ModelTransformScale copyTo(final _ModelTransformScale pojo) {
        float[] thirdPersonScale; {
            thirdPersonScale = this.thirdPersonScale != null ? this.thirdPersonScale.clone() : null;
        } pojo.setThirdPersonScale(thirdPersonScale);
        float[] groundScale; {
            groundScale = this.groundScale != null ? this.groundScale.clone() : null;
        } pojo.setGroundScale(groundScale);
        float[] fixedScale; {
            fixedScale = this.fixedScale != null ? this.fixedScale.clone() : null;
        } pojo.setFixedScale(fixedScale);
        return pojo;
    }

    @Override
    public _ModelTransformScale copy() {
        _ModelTransformScale pojo = copyTo(new _ModelTransformScale());
        pojo.setValid(this.isValid());
        return pojo;
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

    // --------Back compatibility--------

    @Override
    public _ModelTransformScale applyBackCompatibility() {
        this.thirdPersonScale = this.thirdPersonScale == null ? new float[]{1f, 1f, 1f} : this.thirdPersonScale;
        this.groundScale = this.groundScale == null ? new float[]{1f, 1f, 1f} : this.groundScale;
        this.fixedScale = this.fixedScale == null ? new float[]{1f, 1f, 1f} : this.fixedScale;
        return this;
    }
}
