package dev.xcolorful.customgun.client.resource.assets.animation;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock._Animation;
import dev.xcolorful.customgun.core.api.resource.assets.animation.BedrockAnimationTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BedrockAnimation extends ResourcePojo<BedrockAnimation> {

    private String formatVersion;
    private Map<String, _Animation> animations;

    private static final BedrockAnimation PARSER = new BedrockAnimation();
    public static BedrockAnimation fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected BedrockAnimation fromJsonReader(JsonReader reader) throws IOException {
        BedrockAnimation pojo = new BedrockAnimation();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case BedrockAnimationTag.FORMAT_VERSION -> pojo.formatVersion = JsonUtils.readString(reader);
                    case BedrockAnimationTag.ANIMATIONS -> pojo.animations = JsonUtils.readString2ObjectMap(reader, _Animation::fromJson);
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

    public static void toJson(JsonWriter writer, BedrockAnimation pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, BedrockAnimationTag.FORMAT_VERSION, this.formatVersion);
            JsonUtils.writeString2ObjectMap(writer, BedrockAnimationTag.ANIMATIONS, this.animations, _Animation::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    @Override
    protected BedrockAnimation copyTo(final BedrockAnimation pojo) {
        pojo.setFormatVersion(this.formatVersion);
        Map<String, _Animation> animations = new HashMap<>(); {
            for (var entry : this.animations.entrySet()) {
                animations.put(entry.getKey(), entry.getValue().copy());
            }
        } pojo.setAnimations(animations);
        return pojo;
    }

    @Override
    public BedrockAnimation copy() {
        BedrockAnimation pojo = copyTo(new BedrockAnimation());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public String getFormatVersion() {
        return formatVersion;
    }
    public Map<String, _Animation> getAnimations() {
        return animations;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }
    public void setAnimations(Map<String, _Animation> animations) {
        this.animations = animations;
    }
}