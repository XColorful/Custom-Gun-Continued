/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.assets.display.ammo._AmmoEntityDisplay;
import xiao.customgun.client.resource.assets.display.ammo._AmmoParticle;
import xiao.customgun.client.resource.assets.display.ammo._ShellDisplay;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.display.AmmoDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.awt.*;
import java.io.IOException;

public final class AmmoDisplay extends _AssetsDisplay<AmmoDisplay> {

    // 模型
    private @Nullable _AmmoEntityDisplay ammoEntityDisplay;
    private @Nullable _ShellDisplay shellDisplay;

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
                    case AmmoDisplayTag.MODEL_TRANSFORM, AmmoDisplayTag.MODEL_TRANSFORM_OLD1 -> pojo.setModelTransform(_ModelTransform.fromJson(reader));
                    case AmmoDisplayTag.TEXTURE_LOCATION, AmmoDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    case AmmoDisplayTag.SLOT_TEXTURE_LOCATION, AmmoDisplayTag.SLOT_TEXTURE_LOCATION_OLD1 -> pojo.setSlotTextureLocation(JsonUtils.readResourceLocation(reader));

                    case AmmoDisplayTag.AMMO_ENTITY_DISPLAY, AmmoDisplayTag.AMMO_ENTITY_DISPLAY_OLD1 -> pojo.ammoEntityDisplay = JsonUtils.read(reader, _AmmoEntityDisplay::fromJson);
                    case AmmoDisplayTag.SHELL_DISPLAY, AmmoDisplayTag.SHELL_DISPLAY_OLD1 -> pojo.shellDisplay = JsonUtils.read(reader, _ShellDisplay::fromJson);

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

            JsonUtils.write(writer, AmmoDisplayTag.MODEL_TRANSFORM, this.getModelTransform(), _ModelTransform::toJson);
            JsonUtils.write(writer, AmmoDisplayTag.AMMO_ENTITY_DISPLAY, this.ammoEntityDisplay, _AmmoEntityDisplay::toJson);
            JsonUtils.write(writer, AmmoDisplayTag.SHELL_DISPLAY, this.shellDisplay, _ShellDisplay::toJson);

            JsonUtils.write(writer, AmmoDisplayTag.AMMO_PARTICLE, this.ammoParticle, _AmmoParticle::toJson);
            JsonUtils.writeColor(writer, AmmoDisplayTag.TRACER_COLOR, this.tracerColor);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        super.validatePojo();
        if (!this.isValid()) return;

        boolean n1 = (this.getModelTransform() == null | this.getSlotTextureLocation() == null | this.ammoParticle == null | this.tracerColor == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        this.getModelTransform().validate();
        if (this.ammoEntityDisplay != null) this.ammoEntityDisplay.validate();
        if (this.shellDisplay != null) this.shellDisplay.validate();
        this.ammoParticle.validate();
        boolean v1 = (this.getModelTransform().isValid() & (this.ammoEntityDisplay == null || this.ammoEntityDisplay.isValid()) & this.ammoParticle.isValid());
        boolean v2 = ((this.shellDisplay == null || this.shellDisplay.isValid()));
        if (!(v1 & v2)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public @Nullable _AmmoEntityDisplay getAmmoEntityDisplay() {
        return ammoEntityDisplay;
    }
    public @Nullable _ShellDisplay getShellDisplay() {
        return shellDisplay;
    }
    public _AmmoParticle getAmmoParticle() {
        return ammoParticle;
    }
    public Color getTracerColor() {
        return tracerColor;
    }

    public void setAmmoEntityDisplay(_AmmoEntityDisplay ammoEntityDisplay) {
        this.ammoEntityDisplay = ammoEntityDisplay;
    }
    public void setShellDisplay(_ShellDisplay shellDisplay) {
        this.shellDisplay = shellDisplay;
    }
    public void setAmmoParticle(_AmmoParticle ammoParticle) {
        this.ammoParticle = ammoParticle;
    }
    public void setTracerColor(Color tracerColor) {
        this.tracerColor = tracerColor;
    }

    // --------Back compatibility--------

    @Override
    public AmmoDisplay applyBackCompatibility() {
        super.applyBackCompatibility();
        this.setModelTransform(this.getModelTransform() == null ? new _ModelTransform().applyBackCompatibility() : this.getModelTransform().applyBackCompatibility());
        this.setSlotTextureLocation(this.getSlotTextureLocation() == null ? ResourceTag.NULL_LOCATION : this.getSlotTextureLocation());

        if (this.ammoEntityDisplay != null) this.ammoEntityDisplay.applyBackCompatibility();
        if (this.shellDisplay != null) this.shellDisplay.applyBackCompatibility();

        this.ammoParticle = this.ammoParticle == null ? new _AmmoParticle().applyBackCompatibility() : this.ammoParticle.applyBackCompatibility();
        this.tracerColor = this.tracerColor == null ? Color.WHITE : this.tracerColor;
        return this;
    }
}