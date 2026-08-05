/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.animation.bedrock;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation._Bone;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation._SoundEffects;
import dev.xcolorful.customgun.core.api.resource.assets.animation.bedrock._AnimationTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;

public final class _Animation extends ResourcePojo<_Animation> {

    private boolean loop;
    private float animationLength;
    private HashMap<String, _Bone> bones;
    private _SoundEffects soundEffects;

    private static final _Animation PARSER = new _Animation();
    public static _Animation fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _Animation fromJsonReader(JsonReader reader) throws IOException {
        _Animation pojo = new _Animation();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _AnimationTag.LOOP -> pojo.loop = JsonUtils.readBoolean(reader);
                    case _AnimationTag.ANIMATION_LENGTH -> pojo.animationLength = JsonUtils.readFloat(reader);
                    case _AnimationTag.BONES -> pojo.bones = JsonUtils.readString2ObjectMap(reader, _Bone::fromJson);
                    case _AnimationTag.SOUND_EFFECTS -> pojo.soundEffects = JsonUtils.read(reader, _SoundEffects::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _Animation pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            if (this.loop) JsonUtils.writeBoolean(writer, _AnimationTag.LOOP, this.loop);
            JsonUtils.writeFloat(writer, _AnimationTag.ANIMATION_LENGTH, this.animationLength);
            JsonUtils.writeString2ObjectMap(writer, _AnimationTag.BONES, this.bones, _Bone::toJson);
            JsonUtils.write(writer, _AnimationTag.SOUND_EFFECTS, this.soundEffects, _SoundEffects::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public boolean getLoop() {
        return loop;
    }
    public float getAnimationLength() {
        return animationLength;
    }
    public HashMap<String, _Bone> getBones() {
        return bones;
    }
    public _SoundEffects getSoundEffects() {
        return soundEffects;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    public void setAnimationLength(float animationLength) {
        this.animationLength = animationLength;
    }
    public void setBones(HashMap<String, _Bone> bones) {
        this.bones = bones;
    }
    public void setSoundEffects(_SoundEffects soundEffects) {
        this.soundEffects = soundEffects;
    }
}