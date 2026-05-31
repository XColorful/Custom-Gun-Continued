/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.ammo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.Identifier;
import xiao.customgun.core.api.resource.assets.display.ammo._AmmoParticleTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _AmmoParticle extends ResourcePojo<_AmmoParticle> {

    private Identifier particleLocation;
    private float[] delta;
    private float speed = 0f;
    private int count = 1;
    private int lifetimeTicks = 20;

    private static final _AmmoParticle PARSER = new _AmmoParticle();
    public static _AmmoParticle fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _AmmoParticle fromJsonReader(JsonReader reader) throws IOException {
        _AmmoParticle pojo = new _AmmoParticle();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _AmmoParticleTag.PARTICLE_LOCATION, _AmmoParticleTag.PARTICLE_LOCATION_OLD1 -> pojo.particleLocation = JsonUtils.readResourceLocation(reader);
                    case _AmmoParticleTag.DELTA -> pojo.delta = JsonUtils.readFloatArrayFast(reader, 3);
                    case _AmmoParticleTag.SPEED -> pojo.speed = JsonUtils.readFloat(reader);
                    case _AmmoParticleTag.COUNT -> pojo.count = JsonUtils.readInt(reader);
                    case _AmmoParticleTag.LIFETIME_TICKS, _AmmoParticleTag.LIFETIME_TICKS_OLD1 -> pojo.lifetimeTicks = JsonUtils.readInt(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _AmmoParticle pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _AmmoParticleTag.PARTICLE_LOCATION, this.particleLocation);
            JsonUtils.writeFloatArray(writer, _AmmoParticleTag.DELTA, this.delta);
            JsonUtils.writeFloat(writer, _AmmoParticleTag.SPEED, this.speed);
            JsonUtils.writeInt(writer, _AmmoParticleTag.COUNT, this.count);
            JsonUtils.writeInt(writer, _AmmoParticleTag.LIFETIME_TICKS, this.lifetimeTicks);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        boolean n1 = (this.particleLocation == null | this.delta == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Identifier getParticleLocation() {
        return particleLocation;
    }
    public float[] getDelta() {
        return delta;
    }
    public float getSpeed() {
        return speed;
    }
    public int getCount() {
        return count;
    }
    public int getLifetimeTicks() {
        return lifetimeTicks;
    }

    public void setParticleLocation(Identifier particleLocation) {
        this.particleLocation = particleLocation;
    }
    public void setDelta(float[] delta) {
        this.delta = delta;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setLifetimeTicks(int lifetimeTicks) {
        this.lifetimeTicks = lifetimeTicks;
    }
}