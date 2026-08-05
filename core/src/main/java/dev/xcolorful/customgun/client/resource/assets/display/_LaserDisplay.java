/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.assets.display._LaserDisplayTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.awt.*;
import java.io.IOException;

public final class _LaserDisplay extends ResourcePojo<_LaserDisplay> {

    private Color defaultColor;
    private boolean enableCustomizedColor = true;
    private float laserLength = 25;
    private float laserWidth = 0.008f;
    private float thirdPersonLaserLength = 2f;
    private float thirdPersonLaserWidth = 0.008f;

    private static final _LaserDisplay PARSER = new _LaserDisplay();
    public static _LaserDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _LaserDisplay fromJsonReader(JsonReader reader) throws IOException {
        _LaserDisplay pojo = new _LaserDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _LaserDisplayTag.DEFAULT_COLOR -> pojo.defaultColor = JsonUtils.readColor(reader);
                    case _LaserDisplayTag.ENABLE_CUSTOMIZED_COLOR, _LaserDisplayTag.ENABLE_CUSTOMIZED_COLOR_OLD1 -> pojo.enableCustomizedColor = JsonUtils.readBoolean(reader);
                    case _LaserDisplayTag.LASER_LENGTH, _LaserDisplayTag.LASER_LENGTH_OLD1 -> pojo.laserLength = JsonUtils.readFloat(reader);
                    case _LaserDisplayTag.LASER_WIDTH, _LaserDisplayTag.LASER_WIDTH_OLD1 -> pojo.laserWidth = JsonUtils.readFloat(reader);
                    case _LaserDisplayTag.THIRD_PERSON_LASER_LENGTH, _LaserDisplayTag.THIRD_PERSON_LASER_LENGTH_OLD1 -> pojo.thirdPersonLaserLength = JsonUtils.readFloat(reader);
                    case _LaserDisplayTag.THIRD_PERSON_LASER_WIDTH, _LaserDisplayTag.THIRD_PERSON_LASER_WIDTH_OLD1 -> pojo.thirdPersonLaserWidth = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _LaserDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeColor(writer, _LaserDisplayTag.DEFAULT_COLOR, this.defaultColor);
            JsonUtils.writeBoolean(writer, _LaserDisplayTag.ENABLE_CUSTOMIZED_COLOR, this.enableCustomizedColor);
            JsonUtils.writeFloat(writer, _LaserDisplayTag.LASER_LENGTH, this.laserLength);
            JsonUtils.writeFloat(writer, _LaserDisplayTag.LASER_WIDTH, this.laserWidth);
            JsonUtils.writeFloat(writer, _LaserDisplayTag.THIRD_PERSON_LASER_LENGTH, this.thirdPersonLaserLength);
            JsonUtils.writeFloat(writer, _LaserDisplayTag.THIRD_PERSON_LASER_WIDTH, this.thirdPersonLaserWidth);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.defaultColor == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Color getDefaultColor() {
        return defaultColor;
    }
    public boolean isEnableCustomizedColor() {
        return enableCustomizedColor;
    }
    public float getLaserLength() {
        return laserLength;
    }
    public float getLaserWidth() {
        return laserWidth;
    }
    public float getThirdPersonLaserLength() {
        return thirdPersonLaserLength;
    }
    public float getThirdPersonLaserWidth() {
        return thirdPersonLaserWidth;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }
    public void setEnableCustomizedColor(boolean enableCustomizedColor) {
        this.enableCustomizedColor = enableCustomizedColor;
    }
    public void setLaserLength(float laserLength) {
        this.laserLength = laserLength;
    }
    public void setLaserWidth(float laserWidth) {
        this.laserWidth = laserWidth;
    }
    public void setThirdPersonLaserLength(float thirdPersonLaserLength) {
        this.thirdPersonLaserLength = thirdPersonLaserLength;
    }
    public void setThirdPersonLaserWidth(float thirdPersonLaserWidth) {
        this.thirdPersonLaserWidth = thirdPersonLaserWidth;
    }

    // --------Back compatibility--------

    @Override
    public _LaserDisplay applyBackCompatibility() {
        this.defaultColor = this.defaultColor == null ? Color.WHITE : this.defaultColor;
        return this;
    }
}