package dev.xcolorful.customgun.client.resource.assets.display.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.display._AssetsDisplay;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.display.gun._MuzzleFlashDisplayTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.Identifier;

import java.io.IOException;

public final class _MuzzleFlashDisplay extends ResourcePojo<_MuzzleFlashDisplay> {

    private Identifier textureLocation;
    private float textureScale = 1;

    private static final _MuzzleFlashDisplay PARSER = new _MuzzleFlashDisplay();
    public static _MuzzleFlashDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _MuzzleFlashDisplay fromJsonReader(JsonReader reader) throws IOException {
        _MuzzleFlashDisplay pojo = new _MuzzleFlashDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _MuzzleFlashDisplayTag.TEXTURE_LOCATION, _MuzzleFlashDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.textureLocation = JsonUtils.readResourceLocation(reader);
                    case _MuzzleFlashDisplayTag.TEXTURE_SCALE, _MuzzleFlashDisplayTag.TEXTURE_SCALE_OLD1 -> pojo.textureScale = JsonUtils.readFloat(reader);
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

    public static void toJson(JsonWriter writer, _MuzzleFlashDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _MuzzleFlashDisplayTag.TEXTURE_LOCATION, this.textureLocation);
            JsonUtils.writeFloat(writer, _MuzzleFlashDisplayTag.TEXTURE_SCALE, this.textureScale);
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

        boolean n1 = (this.textureLocation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected _MuzzleFlashDisplay copyTo(final _MuzzleFlashDisplay pojo) {
        pojo.setTextureLocation(this.textureLocation);
        pojo.setTextureScale(this.textureScale);
        return pojo;
    }

    @Override
    public _MuzzleFlashDisplay copy() {
        _MuzzleFlashDisplay pojo = copyTo(new _MuzzleFlashDisplay());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public Identifier getTextureLocation() {
        return textureLocation;
    }
    public float getTextureScale() {
        return textureScale;
    }

    public void setTextureLocation(Identifier textureLocation) {
        this.textureLocation = textureLocation;
    }
    public void setTextureScale(float textureScale) {
        this.textureScale = textureScale;
    }

    // --------Back compatibility--------

    @Override
    public _MuzzleFlashDisplay applyBackCompatibility() {
        this.textureLocation = _AssetsDisplay.applyTextureLocationBackCompatibility(this.textureLocation);
        this.textureLocation = this.textureLocation == null ? ResourceTag.NULL_LOCATION : this.textureLocation;
        return this;
    }
}