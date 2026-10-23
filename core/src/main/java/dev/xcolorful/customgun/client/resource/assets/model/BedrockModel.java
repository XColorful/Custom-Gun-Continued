package dev.xcolorful.customgun.client.resource.assets.model;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock._GeometryModel;
import dev.xcolorful.customgun.core.api.resource.assets.model.BedrockModelTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BedrockModel extends ResourcePojo<BedrockModel> {

    private String formatVersion;
    private List<_GeometryModel> geometryModels;

    private static final BedrockModel PARSER = new BedrockModel();
    public static BedrockModel fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected BedrockModel fromJsonReader(JsonReader reader) throws IOException {
        BedrockModel pojo = new BedrockModel();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case BedrockModelTag.FORMAT_VERSION -> pojo.formatVersion = JsonUtils.readString(reader);
                    case BedrockModelTag.GEOMETRY_MODEL -> pojo.geometryModels = JsonUtils.readList(reader, _GeometryModel::fromJson);
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

    public static void toJson(JsonWriter writer, BedrockModel pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, BedrockModelTag.FORMAT_VERSION, this.formatVersion);
            JsonUtils.writeList(writer, BedrockModelTag.GEOMETRY_MODEL, this.geometryModels, _GeometryModel::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    @Override
    protected BedrockModel copyTo(final BedrockModel pojo) {
        pojo.setFormatVersion(this.formatVersion);
        List<_GeometryModel> geometryModels = new ArrayList<>(this.geometryModels.size()); {
            for (int i = 0; i < this.geometryModels.size(); i++) {
                geometryModels.add(this.geometryModels.get(i).copy());
            }
        } pojo.setGeometryModels(geometryModels);
        return pojo;
    }

    @Override
    public BedrockModel copy() {
        BedrockModel pojo = copyTo(new BedrockModel());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public String getFormatVersion() {
        return formatVersion;
    }
    public List<_GeometryModel> getGeometryModels() {
        return geometryModels;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }
    public void setGeometryModels(List<_GeometryModel> geometryModels) {
        this.geometryModels = geometryModels;
    }

    // --------Special--------

    @Deprecated(forRemoval = true) public @Nullable _GeometryModel getGeometryModelNew() {
        if (this.geometryModels == null) return null;
        return this.geometryModels.get(0);
    }
}