/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.Identifier;
import xiao.customgun.client.resource.assets.display.ammo._AmmoParticle;
import xiao.customgun.core.api.resource.assets.display.AmmoDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.awt.*;
import java.io.IOException;

public final class AmmoDisplay extends _AssetsDisplay<AmmoDisplay> {

    // 模型
    private Identifier ammoEntityDisplayLocation;
    private Identifier shellDisplayLocation;

    // 显示
    private _AmmoParticle ammoParticle;
    private Color tracerColor;

    private static final AmmoDisplay PARSER = new AmmoDisplay();
    public static AmmoDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AmmoDisplay fromJsonReader(JsonReader reader) throws IOException {
        AmmoDisplay pojo = new AmmoDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AmmoDisplayTag.MODEL_LOCATION, AmmoDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case AmmoDisplayTag.TEXTURE_LOCATION, AmmoDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    case AmmoDisplayTag.SLOT_TEXTURE_LOCATION, AmmoDisplayTag.SLOT_TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));

                    case AmmoDisplayTag.TRANSFORM_SCALE, AmmoDisplayTag.TRANSFORM_SCALE_OLD1 -> pojo.setTransformScale(_TransformScale.fromJson(reader));
                    case AmmoDisplayTag.AMMO_ENTITY_DISPLAY_LOCATION, AmmoDisplayTag.AMMO_ENTITY_DISPLAY_LOCATION_OLD1 -> pojo.ammoEntityDisplayLocation = JsonUtils.readResourceLocation(reader);
                    case AmmoDisplayTag.SHELL_DISPLAY_LOCATION, AmmoDisplayTag.SHELL_DISPLAY_LOCATION_OLD1 -> pojo.shellDisplayLocation = JsonUtils.readResourceLocation(reader);

                    case AmmoDisplayTag.AMMO_PARTICLE, AmmoDisplayTag.AMMO_PARTICLE_OLD1 -> pojo.ammoParticle = _AmmoParticle.fromJson(reader);
                    case AmmoDisplayTag.TRACER_COLOR -> pojo.tracerColor = JsonUtils.readColor(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AmmoDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, AmmoDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, AmmoDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
            JsonUtils.writeResourceLocation(writer, AmmoDisplayTag.SLOT_TEXTURE_LOCATION, this.getSlotTextureLocation());

            JsonUtils.write(writer, AmmoDisplayTag.TRANSFORM_SCALE, this.getTransformScale(), _TransformScale::toJson);
            JsonUtils.writeResourceLocation(writer, AmmoDisplayTag.AMMO_ENTITY_DISPLAY_LOCATION, this.ammoEntityDisplayLocation);
            JsonUtils.writeResourceLocation(writer, AmmoDisplayTag.SHELL_DISPLAY_LOCATION, this.shellDisplayLocation);

            JsonUtils.write(writer, AmmoDisplayTag.AMMO_PARTICLE, this.ammoParticle, _AmmoParticle::toJson);
            JsonUtils.writeColor(writer, AmmoDisplayTag.TRACER_COLOR, this.tracerColor);
        }
        writer.endObject();
    }

    // --------Getter & Setter--------

    public Identifier getAmmoEntityDisplayLocation() {
        return ammoEntityDisplayLocation;
    }
    public Identifier getShellDisplayLocation() {
        return shellDisplayLocation;
    }
    public _AmmoParticle getAmmoParticle() {
        return ammoParticle;
    }
    public Color getTracerColor() {
        return tracerColor;
    }

    public void setAmmoEntityDisplayLocation(Identifier ammoEntityDisplayLocation) {
        this.ammoEntityDisplayLocation = ammoEntityDisplayLocation;
    }
    public void setShellDisplayLocation(Identifier shellDisplayLocation) {
        this.shellDisplayLocation = shellDisplayLocation;
    }
    public void setAmmoParticle(_AmmoParticle ammoParticle) {
        this.ammoParticle = ammoParticle;
    }
    public void setTracerColor(Color tracerColor) {
        this.tracerColor = tracerColor;
    }
}