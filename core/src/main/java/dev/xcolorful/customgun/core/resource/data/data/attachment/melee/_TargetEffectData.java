/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.attachment.melee;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.data.data.attachment.melee._TargetEffectDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.Identifier;

import java.io.IOException;

public final class _TargetEffectData extends ResourcePojo<_TargetEffectData> {

    private Identifier effectLocation;
    private int seconds = 0;
    private int amplifier = 0;
    private boolean hideParticles = false;

    private static final _TargetEffectData PARSER = new _TargetEffectData();
    public static _TargetEffectData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _TargetEffectData fromJsonReader(JsonReader reader) throws IOException {
        _TargetEffectData pojo = new _TargetEffectData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _TargetEffectDataTag.EFFECT_LOCATION, _TargetEffectDataTag.EFFECT_LOCATION_OLD1 -> pojo.effectLocation = JsonUtils.readResourceLocation(reader);
                    case _TargetEffectDataTag.SECONDS, _TargetEffectDataTag.SECONDS_OLD1 -> pojo.seconds = JsonUtils.readInt(reader);
                    case _TargetEffectDataTag.AMPLIFIER -> pojo.amplifier = JsonUtils.readInt(reader);
                    case _TargetEffectDataTag.HIDE_PARTICLES -> pojo.hideParticles = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _TargetEffectData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _TargetEffectDataTag.EFFECT_LOCATION, this.effectLocation);
            JsonUtils.writeInt(writer, _TargetEffectDataTag.SECONDS, this.seconds);
            JsonUtils.writeInt(writer, _TargetEffectDataTag.AMPLIFIER, this.amplifier);
            JsonUtils.writeBoolean(writer, _TargetEffectDataTag.HIDE_PARTICLES, this.hideParticles);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.effectLocation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Identifier getEffectLocation() {
        return effectLocation;
    }
    public int getSeconds() {
        return seconds;
    }
    public int getAmplifier() {
        return amplifier;
    }
    public boolean getHideParticles() {
        return hideParticles;
    }

    public void setEffectLocation(Identifier effectLocation) {
        this.effectLocation = effectLocation;
    }
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }
    public void setHideParticles(boolean hideParticles) {
        this.hideParticles = hideParticles;
    }

    // --------Back compatibility--------

    @Override
    public _TargetEffectData applyBackCompatibility() {
        this.effectLocation = this.effectLocation == null ? ResourceTag.NULL_LOCATION : this.effectLocation;
        return this;
    }
}