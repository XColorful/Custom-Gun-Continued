package dev.xcolorful.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.attachment._RecoilDataModifierDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class _RecoilDataModifierData extends ResourcePojo<_RecoilDataModifierData> {

    private @Nullable _SimpleModifierData pitchRecoilModifier;
    private @Nullable _SimpleModifierData yawRecoilModifier;

    private static final _RecoilDataModifierData PARSER = new _RecoilDataModifierData();
    public static _RecoilDataModifierData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _RecoilDataModifierData fromJsonReader(JsonReader reader) throws IOException {
        _RecoilDataModifierData pojo = new _RecoilDataModifierData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _RecoilDataModifierDataTag.PITCH_RECOIL, _RecoilDataModifierDataTag.PITCH_RECOIL_OLD1 -> pojo.pitchRecoilModifier = JsonUtils.read(reader, _SimpleModifierData::fromJson);
                    case _RecoilDataModifierDataTag.YAW_RECOIL, _RecoilDataModifierDataTag.YAW_RECOIL_OLD1 -> pojo.yawRecoilModifier = JsonUtils.read(reader, _SimpleModifierData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _RecoilDataModifierData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, _RecoilDataModifierDataTag.PITCH_RECOIL, this.pitchRecoilModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, _RecoilDataModifierDataTag.YAW_RECOIL, this.yawRecoilModifier, _SimpleModifierData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (this.pitchRecoilModifier != null) this.pitchRecoilModifier.validate();
        if (this.yawRecoilModifier != null) this.yawRecoilModifier.validate();
        boolean v1 = ((this.pitchRecoilModifier == null || this.pitchRecoilModifier.isValid()) & (this.yawRecoilModifier == null || this.yawRecoilModifier.isValid()));
        if (!(v1)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected _RecoilDataModifierData copyTo(final _RecoilDataModifierData pojo) {
        @Nullable _SimpleModifierData pitchRecoilModifier = null; {
            if (this.pitchRecoilModifier != null) pitchRecoilModifier = this.pitchRecoilModifier.copy();
        } pojo.setPitchRecoilModifier(pitchRecoilModifier);
        @Nullable _SimpleModifierData yawRecoilModifier = null; {
            if (this.yawRecoilModifier != null) yawRecoilModifier = this.yawRecoilModifier.copy();
        } pojo.setYawRecoilModifier(yawRecoilModifier);
        return pojo;
    }

    @Override
    public _RecoilDataModifierData copy() {
        _RecoilDataModifierData pojo = copyTo(new _RecoilDataModifierData());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public @Nullable _SimpleModifierData getPitchRecoilModifier() {
        return pitchRecoilModifier;
    }
    public @Nullable _SimpleModifierData getYawRecoilModifier() {
        return yawRecoilModifier;
    }

    public void setPitchRecoilModifier(_SimpleModifierData pitchRecoilModifier) {
        this.pitchRecoilModifier = pitchRecoilModifier;
    }
    public void setYawRecoilModifier(_SimpleModifierData yawRecoilModifier) {
        this.yawRecoilModifier = yawRecoilModifier;
    }

    // --------Back compatibility--------

    @Override
    public _RecoilDataModifierData applyBackCompatibility() {
        if (this.pitchRecoilModifier != null) this.pitchRecoilModifier.applyBackCompatibility();
        if (this.yawRecoilModifier != null) this.yawRecoilModifier.applyBackCompatibility();
        return this;
    }
}