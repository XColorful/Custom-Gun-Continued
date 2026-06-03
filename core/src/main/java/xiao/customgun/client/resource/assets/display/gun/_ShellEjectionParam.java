/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.assets.display.gun._ShellEjectionParamTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ShellEjectionParam extends ResourcePojo<_ShellEjectionParam> {

    private float[] baseVelocity;
    private float[] randomizeVelocity;
    private float[] acceleration;
    private float[] angularVelocity;
    private float lifetimeSeconds = 1;

    private static final _ShellEjectionParam PARSER = new _ShellEjectionParam();
    public static _ShellEjectionParam fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ShellEjectionParam fromJsonReader(JsonReader reader) throws IOException {
        _ShellEjectionParam pojo = new _ShellEjectionParam();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ShellEjectionParamTag.BASE_VELOCITY, _ShellEjectionParamTag.BASE_VELOCITY_OLD1 -> pojo.baseVelocity = JsonUtils.readFloatArrayFast(reader, 3);
                    case _ShellEjectionParamTag.RANDOMIZE_VELOCITY, _ShellEjectionParamTag.RANDOMIZE_VELOCITY_OLD1 -> pojo.randomizeVelocity = JsonUtils.readFloatArrayFast(reader, 3);
                    case _ShellEjectionParamTag.ACCELERATION -> pojo.acceleration = JsonUtils.readFloatArrayFast(reader, 3);
                    case _ShellEjectionParamTag.ANGULAR_VELOCITY -> pojo.angularVelocity = JsonUtils.readFloatArrayFast(reader, 3);
                    case _ShellEjectionParamTag.LIFETIME_SECONDS, _ShellEjectionParamTag.LIFETIME_SECONDS_OLD1 -> pojo.lifetimeSeconds = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ShellEjectionParam pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloatArray(writer, _ShellEjectionParamTag.BASE_VELOCITY, this.baseVelocity);
            JsonUtils.writeFloatArray(writer, _ShellEjectionParamTag.RANDOMIZE_VELOCITY, this.randomizeVelocity);
            JsonUtils.writeFloatArray(writer, _ShellEjectionParamTag.ACCELERATION, this.acceleration);
            JsonUtils.writeFloatArray(writer, _ShellEjectionParamTag.ANGULAR_VELOCITY, this.angularVelocity);
            JsonUtils.writeFloat(writer, _ShellEjectionParamTag.LIFETIME_SECONDS, this.lifetimeSeconds);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.baseVelocity == null | this.randomizeVelocity == null | this.acceleration == null | this.angularVelocity == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float[] getBaseVelocity() {
        return baseVelocity;
    }
    public float[] getRandomizeVelocity() {
        return randomizeVelocity;
    }
    public float[] getAcceleration() {
        return acceleration;
    }
    public float[] getAngularVelocity() {
        return angularVelocity;
    }
    public float getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public void setBaseVelocity(float[] baseVelocity) {
        this.baseVelocity = baseVelocity;
    }
    public void setRandomizeVelocity(float[] randomizeVelocity) {
        this.randomizeVelocity = randomizeVelocity;
    }
    public void setAcceleration(float[] acceleration) {
        this.acceleration = acceleration;
    }
    public void setAngularVelocity(float[] angularVelocity) {
        this.angularVelocity = angularVelocity;
    }
    public void setLifetimeSeconds(float lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }

    // --------Back compatibility--------

    @Override
    public _ShellEjectionParam applyBackCompatibility() {
        this.baseVelocity = this.baseVelocity == null ? new float[]{0f, 0f, 0f} : this.baseVelocity;
        this.randomizeVelocity = this.randomizeVelocity == null ? new float[]{0f, 0f, 0f} : this.randomizeVelocity;
        this.acceleration = this.acceleration == null ? new float[]{0f, 0f, 0f} : this.acceleration;
        this.angularVelocity = this.angularVelocity == null ? new float[]{0f, 0f, 0f} : this.angularVelocity;
        return this;
    }
}