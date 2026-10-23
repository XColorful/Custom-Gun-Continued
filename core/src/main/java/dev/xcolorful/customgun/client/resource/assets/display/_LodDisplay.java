package dev.xcolorful.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.display._AssetsDisplayTag;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _LodDisplay extends _AssetsDisplay<_LodDisplay> {

    private static final _LodDisplay PARSER = new _LodDisplay();
    public static _LodDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _LodDisplay fromJsonReader(JsonReader reader) throws IOException {
        _LodDisplay pojo = new _LodDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _AssetsDisplayTag.MODEL_LOCATION, _AssetsDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case _AssetsDisplayTag.TEXTURE_LOCATION, _AssetsDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
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

    public static void toJson(JsonWriter writer, _LodDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _AssetsDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, _AssetsDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
        }
        writer.endObject();
    }

    @Override
    protected _LodDisplay copyTo(final _LodDisplay pojo) {
        super.copyTo(pojo);

        return pojo;
    }

    @Override
    public _LodDisplay copy() {
        _LodDisplay pojo = copyTo(new _LodDisplay());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    // --------Back compatibility--------

    @Override
    public _LodDisplay applyBackCompatibility() {
        super.applyBackCompatibility();
        return this;
    }
}