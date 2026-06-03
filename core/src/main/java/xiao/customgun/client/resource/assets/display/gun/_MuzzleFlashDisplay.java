/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.display.gun._MuzzleFlashDisplayTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _MuzzleFlashDisplay extends ResourcePojo<_MuzzleFlashDisplay> {

    private ResourceLocation textureLocation;
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
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _MuzzleFlashDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _MuzzleFlashDisplayTag.TEXTURE_LOCATION, this.textureLocation);
            JsonUtils.writeFloat(writer, _MuzzleFlashDisplayTag.TEXTURE_SCALE, this.textureScale);
        }
        writer.endObject();
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

    // --------Getter & Setter--------

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }
    public float getTextureScale() {
        return textureScale;
    }

    public void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }
    public void setTextureScale(float textureScale) {
        this.textureScale = textureScale;
    }

    // --------Back compatibility--------

    @Override
    public _MuzzleFlashDisplay applyBackCompatibility() {
        this.textureLocation = this.textureLocation == null ? ResourceTag.NULL_LOCATION : this.textureLocation;
        return this;
    }
}