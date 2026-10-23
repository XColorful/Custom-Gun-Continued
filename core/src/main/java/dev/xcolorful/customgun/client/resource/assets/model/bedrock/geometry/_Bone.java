package dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone._Cube;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry._BoneTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
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
        this.setValid(true);
    }

    @Override
    protected _Bone copyTo(final _Bone pojo) {
        pojo.setName(this.name);
        pojo.setParent(this.parent);
        float[] pivot; {
            pivot = this.pivot != null ? this.pivot.clone() : null;
        } pojo.setPivot(pivot);
        float[] rotation; {
            rotation = this.rotation != null ? this.rotation.clone() : null;
        } pojo.setRotation(rotation);
        @Nullable List<_Cube> cubes = null; {
            if (this.cubes != null) {
                cubes = new ArrayList<>(this.cubes.size());
                for (int i = 0; i < this.cubes.size(); i++) {
                    cubes.add(this.cubes.get(i).copy());
                }
            }
        } pojo.setCubes(cubes);
        pojo.setMirror(this.mirror);
        return pojo;
    }

    @Override
    public _Bone copy() {
        _Bone pojo = copyTo(new _Bone());
        pojo.setValid(this.isValid());
        return pojo;
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
    public float @Nullable [] getRotation() {
        return rotation;
    }
    public @Nullable List<_Cube> getCubes() {
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