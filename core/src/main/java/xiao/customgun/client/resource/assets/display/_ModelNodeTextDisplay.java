/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.network.chat.MutableComponent;
import xiao.customgun.core.api.resource.assets.display._ModelNodeTextDisplayTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ModelNodeTextDisplay extends ResourcePojo<_ModelNodeTextDisplay> {

    private MutableComponent textLang;
    private float textScale;
    private int textColor = 0xFFFFFF;
    private int textLight = 15;
    private boolean enableTextShadow = false;
    private float xOffsetScale;

    private static final _ModelNodeTextDisplay PARSER = new _ModelNodeTextDisplay();
    public static _ModelNodeTextDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ModelNodeTextDisplay fromJsonReader(JsonReader reader) throws IOException {
        _ModelNodeTextDisplay pojo = new _ModelNodeTextDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ModelNodeTextDisplayTag.TEXT_LANG, _ModelNodeTextDisplayTag.TEXT_LANG_OLD1 -> pojo.textLang = JsonUtils.readTranslatable(reader);
                    case _ModelNodeTextDisplayTag.TEXT_SCALE, _ModelNodeTextDisplayTag.TEXT_SCALE_OLD1 -> pojo.textScale = JsonUtils.readFloat(reader);
                    case _ModelNodeTextDisplayTag.TEXT_COLOR, _ModelNodeTextDisplayTag.TEXT_COLOR_OLD1 -> pojo.textColor = JsonUtils.readColorInt(reader);
                    case _ModelNodeTextDisplayTag.TEXT_LIGHT, _ModelNodeTextDisplayTag.TEXT_LIGHT_OLD1 -> pojo.textLight = JsonUtils.readInt(reader);
                    case _ModelNodeTextDisplayTag.ENABLE_TEXT_SHADOW, _ModelNodeTextDisplayTag.ENABLE_TEXT_SHADOW_OLD1 -> pojo.enableTextShadow = JsonUtils.readBoolean(reader);
                    case _ModelNodeTextDisplayTag.X_OFFSET_SCALE -> pojo.xOffsetScale = JsonUtils.readFloat(reader); case _ModelNodeTextDisplayTag.X_OFFSET_SCALE_OLD1 -> {String s = JsonUtils.readString(reader); pojo.xOffsetScale = s != null ? switch (s) {case "left" -> 0; case "right" -> 1; default -> 0.5f;} : 0.5f;}
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ModelNodeTextDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeTranslatable(writer, _ModelNodeTextDisplayTag.TEXT_LANG, this.textLang);
            JsonUtils.writeFloat(writer, _ModelNodeTextDisplayTag.TEXT_SCALE, this.textScale);
            JsonUtils.writeColorInt(writer, _ModelNodeTextDisplayTag.TEXT_COLOR, this.textColor);
            JsonUtils.writeInt(writer, _ModelNodeTextDisplayTag.TEXT_LIGHT, this.textLight);
            JsonUtils.writeBoolean(writer, _ModelNodeTextDisplayTag.ENABLE_TEXT_SHADOW, this.enableTextShadow);
            JsonUtils.writeFloat(writer, _ModelNodeTextDisplayTag.X_OFFSET_SCALE, this.xOffsetScale);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        boolean n1 = (this.textLang == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public MutableComponent getTextLang() {
        return textLang;
    }
    public float getTextScale() {
        return textScale;
    }
    public int getTextColor() {
        return textColor;
    }
    public int getTextLight() {
        return textLight;
    }
    public boolean isEnableTextShadow() {
        return enableTextShadow;
    }
    public float getXOffsetScale() {
        return xOffsetScale;
    }

    public void setTextLang(MutableComponent textLang) {
        this.textLang = textLang;
    }
    public void setTextScale(float textScale) {
        this.textScale = textScale;
    }
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
    public void setTextLight(int textLight) {
        this.textLight = textLight;
    }
    public void setEnableTextShadow(boolean enableTextShadow) {
        this.enableTextShadow = enableTextShadow;
    }
    public void setXOffsetScale(float xOffsetScale) {
        this.xOffsetScale = xOffsetScale;
    }
}