package dev.xcolorful.customgun.client.resource.assets.display.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.display.gun._SurroundDisplayTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _SurroundDisplay extends ResourcePojo<_SurroundDisplay> {

    private float[] pos;
    private float[] rotate;
    private float[] scale;

    private static final _SurroundDisplay PARSER = new _SurroundDisplay();
    public static _SurroundDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _SurroundDisplay fromJsonReader(JsonReader reader) throws IOException {
        _SurroundDisplay pojo = new _SurroundDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _SurroundDisplayTag.POS -> pojo.pos = JsonUtils.readFloatArrayFast(reader, 3);
                    case _SurroundDisplayTag.ROTATE -> pojo.rotate = JsonUtils.readFloatArrayFast(reader, 3);
                    case _SurroundDisplayTag.SCALE -> pojo.scale = JsonUtils.readFloatArrayFast(reader, 3);
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

    public static void toJson(JsonWriter writer, _SurroundDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloatArray(writer, _SurroundDisplayTag.POS, this.pos);
            JsonUtils.writeFloatArray(writer, _SurroundDisplayTag.ROTATE, this.rotate);
            JsonUtils.writeFloatArray(writer, _SurroundDisplayTag.SCALE, this.scale);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.pos == null | this.rotate == null | this.scale == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected _SurroundDisplay copyTo(final _SurroundDisplay pojo) {
        float[] pos; {
            pos = this.pos != null ? this.pos.clone() : null;
        } pojo.setPos(pos);
        float[] rotate; {
            rotate = this.rotate != null ? this.rotate.clone() : null;
        } pojo.setRotate(rotate);
        float[] scale; {
            scale = this.scale != null ? this.scale.clone() : null;
        } pojo.setScale(scale);
        return pojo;
    }

    @Override
    public _SurroundDisplay copy() {
        _SurroundDisplay pojo = copyTo(new _SurroundDisplay());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public float[] getPos() {
        return pos;
    }
    public float[] getRotate() {
        return rotate;
    }
    public float[] getScale() {
        return scale;
    }

    public void setPos(float[] pos) {
        this.pos = pos;
    }
    public void setRotate(float[] rotate) {
        this.rotate = rotate;
    }
    public void setScale(float[] scale) {
        this.scale = scale;
    }

    // --------Back compatibility--------

    @Override
    public _SurroundDisplay applyBackCompatibility() {
        this.pos = this.pos == null ? new float[]{0f, 0f, 0f} : this.pos;
        this.rotate = this.rotate == null ? new float[]{0f, 0f, 0f} : this.rotate;
        this.scale = this.scale == null ? new float[]{1f, 1f, 1f} : this.scale;
        return this;
    }
}