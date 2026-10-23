package dev.xcolorful.customgun.core.resource.data.data.gun.recoil;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun.recoil._RecoilEntryDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _RecoilEntryData extends ResourcePojo<_RecoilEntryData> {

    private float time = 0;
    private float[] range;

    private static final _RecoilEntryData PARSER = new _RecoilEntryData();
    public static _RecoilEntryData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _RecoilEntryData fromJsonReader(JsonReader reader) throws IOException {
        _RecoilEntryData pojo = new _RecoilEntryData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _RecoilEntryDataTag.TIME -> pojo.time = JsonUtils.readFloat(reader);
                    case _RecoilEntryDataTag.RANGE, _RecoilEntryDataTag.RANGE_OLD1 -> pojo.range = JsonUtils.readFloatArrayFast(reader, 2);
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

    public static void toJson(JsonWriter writer, _RecoilEntryData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _RecoilEntryDataTag.TIME, this.time);
            JsonUtils.writeFloatArray(writer, _RecoilEntryDataTag.RANGE, this.range);
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
        boolean n1 = (this.range == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected _RecoilEntryData copyTo(final _RecoilEntryData pojo) {
        pojo.setTime(this.time);
        float[] range; {
            range = this.range != null ? this.range.clone() : null;
        } pojo.setRange(range);
        return pojo;
    }

    @Override
    public _RecoilEntryData copy() {
        _RecoilEntryData pojo = copyTo(new _RecoilEntryData());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public float getTime() {
        return time;
    }
    public float[] getRange() {
        return range;
    }

    public void setTime(float time) {
        this.time = time;
    }
    public void setRange(float[] range) {
        this.range = range;
    }

    // --------Back compatibility--------
}