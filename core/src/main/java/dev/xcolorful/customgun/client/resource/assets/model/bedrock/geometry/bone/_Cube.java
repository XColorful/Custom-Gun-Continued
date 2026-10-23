package dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube._Uv;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.bone._CubeTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

public final class _Cube extends ResourcePojo<_Cube> {

    private float[] origin;
    private float[] size;
    private float inflate;
    private float[] pivot;
    private float[] rotation;
    private _Uv uv;
    private boolean mirror = false;

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
                    case _CubeTag.UV -> pojo.uv = JsonUtils.read(reader, _Uv::fromJson);
                    case _CubeTag.MIRROR -> { pojo.mirror = JsonUtils.readBoolean(reader); pojo.hasMirror = true; }
                    default -> readUnknownKey(reader, key);
                }
            }
        }
        reader.endObject();
        return pojo;
    }
    @Override
    protected boolean readUnknownKey(JsonReader reader, String key) throws IOException {
        return super.readUnknownKey(reader, key);
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

    @Override
    protected _Cube copyTo(final _Cube pojo) {
        float[] origin; {
            origin = this.origin != null ? this.origin.clone() : null;
        } pojo.setOrigin(origin);
        float[] size; {
            size = this.size != null ? this.size.clone() : null;
        } pojo.setSize(size);
        pojo.setInflate(this.inflate);
        float[] pivot; {
            pivot = this.pivot != null ? this.pivot.clone() : null;
        } pojo.setPivot(pivot);
        float[] rotation; {
            rotation = this.rotation != null ? this.rotation.clone() : null;
        } pojo.setRotation(rotation);
        pojo.setUv(this.uv.copy());
        pojo.setMirror(this.mirror);

        pojo.hasMirror = this.hasMirror;
        return pojo;
    }

    @Override
    public _Cube copy() {
        _Cube pojo = copyTo(new _Cube());
        pojo.setValid(this.isValid());
        return pojo;
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
    public boolean getMirror() {
        return mirror;
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
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    // --------Special--------

    @ApiStatus.Internal
    private boolean hasMirror = false;
    public boolean hasMirror() {
        return hasMirror;
    }
}