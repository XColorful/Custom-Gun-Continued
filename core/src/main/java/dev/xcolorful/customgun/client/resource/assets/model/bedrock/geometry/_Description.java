/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry._DescriptionTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _Description extends ResourcePojo<_Description> {

    private String identifier;
    private int textureWidth;
    private int textureHeight;
    private float visibleBoundsWidth;
    private float visibleBoundsHeight;
    private float[] visibleBoundsOffset;

    private static final _Description PARSER = new _Description();
    public static _Description fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _Description fromJsonReader(JsonReader reader) throws IOException {
        _Description pojo = new _Description();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _DescriptionTag.IDENTIFIER -> pojo.identifier = JsonUtils.readString(reader);
                    case _DescriptionTag.TEXTURE_WIDTH -> pojo.textureWidth = JsonUtils.readInt(reader);
                    case _DescriptionTag.TEXTURE_HEIGHT -> pojo.textureHeight = JsonUtils.readInt(reader);
                    case _DescriptionTag.VISIBLE_BOUNDS_WIDTH -> pojo.visibleBoundsWidth = JsonUtils.readFloat(reader);
                    case _DescriptionTag.VISIBLE_BOUNDS_HEIGHT -> pojo.visibleBoundsHeight = JsonUtils.readFloat(reader);
                    case _DescriptionTag.VISIBLE_BOUNDS_OFFSET -> pojo.visibleBoundsOffset = JsonUtils.readFloatArrayFast(reader, 3);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _Description pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, _DescriptionTag.IDENTIFIER, this.identifier);
            JsonUtils.writeInt(writer, _DescriptionTag.TEXTURE_WIDTH, this.textureWidth);
            JsonUtils.writeInt(writer, _DescriptionTag.TEXTURE_HEIGHT, this.textureHeight);
            JsonUtils.writeFloat(writer, _DescriptionTag.VISIBLE_BOUNDS_WIDTH, this.visibleBoundsWidth);
            JsonUtils.writeFloat(writer, _DescriptionTag.VISIBLE_BOUNDS_HEIGHT, this.visibleBoundsHeight);
            JsonUtils.writeFloatArray(writer, _DescriptionTag.VISIBLE_BOUNDS_OFFSET, this.visibleBoundsOffset);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getIdentifier() {
        return identifier;
    }
    public int getTextureWidth() {
        return textureWidth;
    }
    public int getTextureHeight() {
        return textureHeight;
    }
    public float getVisibleBoundsWidth() {
        return visibleBoundsWidth;
    }
    public float getVisibleBoundsHeight() {
        return visibleBoundsHeight;
    }
    public float[] getVisibleBoundsOffset() {
        return visibleBoundsOffset;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }
    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }
    public void setVisibleBoundsWidth(float visibleBoundsWidth) {
        this.visibleBoundsWidth = visibleBoundsWidth;
    }
    public void setVisibleBoundsHeight(float visibleBoundsHeight) {
        this.visibleBoundsHeight = visibleBoundsHeight;
    }
    public void setVisibleBoundsOffset(float[] visibleBoundsOffset) {
        this.visibleBoundsOffset = visibleBoundsOffset;
    }
}