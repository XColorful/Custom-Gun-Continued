/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.display.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._AmmoParticle;
import dev.xcolorful.customgun.core.api.resource.assets.display.gun._AmmoDisplayOverrideTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;

public final class _AmmoDisplayOverride extends ResourcePojo<_AmmoDisplayOverride> {

    // 显示
    private @Nullable _AmmoParticle ammoParticle;
    private Color tracerColor;

    private static final _AmmoDisplayOverride PARSER = new _AmmoDisplayOverride();
    public static _AmmoDisplayOverride fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _AmmoDisplayOverride fromJsonReader(JsonReader reader) throws IOException {
        _AmmoDisplayOverride pojo = new _AmmoDisplayOverride();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _AmmoDisplayOverrideTag.AMMO_PARTICLE, _AmmoDisplayOverrideTag.AMMO_PARTICLE_OLD1 -> pojo.ammoParticle = JsonUtils.read(reader, _AmmoParticle::fromJson);
                    case _AmmoDisplayOverrideTag.TRACER_COLOR -> pojo.tracerColor = JsonUtils.readColor(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _AmmoDisplayOverride pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, _AmmoDisplayOverrideTag.AMMO_PARTICLE, this.ammoParticle, _AmmoParticle::toJson);
            JsonUtils.writeColor(writer, _AmmoDisplayOverrideTag.TRACER_COLOR, this.tracerColor);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.tracerColor == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        if (this.ammoParticle != null) this.ammoParticle.validate();
        boolean v1 = ((this.ammoParticle == null || this.ammoParticle.isValid()));
        if (!v1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public @Nullable _AmmoParticle getAmmoParticle() {
        return ammoParticle;
    }
    public Color getTracerColor() {
        return tracerColor;
    }

    public void setAmmoParticle(_AmmoParticle ammoParticle) {
        this.ammoParticle = ammoParticle;
    }
    public void setTracerColor(Color tracerColor) {
        this.tracerColor = tracerColor;
    }

    // --------Back compatibility--------

    @Override
    public _AmmoDisplayOverride applyBackCompatibility() {
        this.ammoParticle = this.ammoParticle == null ? new _AmmoParticle().applyBackCompatibility() : this.ammoParticle.applyBackCompatibility();
        this.tracerColor = this.tracerColor == null ? Color.WHITE : this.tracerColor;
        return this;
    }
}