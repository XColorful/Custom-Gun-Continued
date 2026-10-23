package dev.xcolorful.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.item.gun.FireSoundType;
import dev.xcolorful.customgun.core.api.resource.data.data.gun._FireSoundDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _FireSoundData extends ResourcePojo<_FireSoundData> {

    /**
     * {@link FireSoundType}
     */
    private float normalMultiplier = 1.0F;
    private float silencedMultiplier = 1.0F;

    private static final _FireSoundData PARSER = new _FireSoundData();
    public static _FireSoundData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _FireSoundData fromJsonReader(JsonReader reader) throws IOException {
        _FireSoundData pojo = new _FireSoundData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FireSoundDataTag.NORMAL_MULTIPLIER, _FireSoundDataTag.NORMAL_MULTIPLIER_OLD1 -> pojo.normalMultiplier = JsonUtils.readFloat(reader);
                    case _FireSoundDataTag.SILENCED_MULTIPLIER, _FireSoundDataTag.SILENCED_MULTIPLIER_OLD1 -> pojo.silencedMultiplier = JsonUtils.readFloat(reader);
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

    public static void toJson(JsonWriter writer, _FireSoundData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _FireSoundDataTag.NORMAL_MULTIPLIER, this.normalMultiplier);
            JsonUtils.writeFloat(writer, _FireSoundDataTag.SILENCED_MULTIPLIER, this.silencedMultiplier);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    @Override
    protected _FireSoundData copyTo(final _FireSoundData pojo) {
        pojo.setNormalMultiplier(this.normalMultiplier);
        pojo.setSilencedMultiplier(this.silencedMultiplier);
        return pojo;
    }

    @Override
    public _FireSoundData copy() {
        _FireSoundData pojo = copyTo(new _FireSoundData());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public float getNormalMultiplier() {
        return normalMultiplier;
    }
    public float getSilencedMultiplier() {
        return silencedMultiplier;
    }

    public void setNormalMultiplier(float normalMultiplier) {
        this.normalMultiplier = normalMultiplier;
    }
    public void setSilencedMultiplier(float silencedMultiplier) {
        this.silencedMultiplier = silencedMultiplier;
    }

    // --------Back compatibility--------
}