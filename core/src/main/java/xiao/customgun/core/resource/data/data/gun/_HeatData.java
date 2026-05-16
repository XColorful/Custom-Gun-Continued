/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._HeatDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _HeatData extends ResourcePojo<_HeatData> {

    // 过热属性
    private float maxHeat = 100F;
    private float heatPerShot = 3.0F;

    // 枪械属性
    private float minRpmByHeat = 1.0F; // 最小热量时的RPM
    private float maxRpmByHeat = 1.0F;
    private float minInaccuracyByHeat = 1.0F; // 最小热量时的不准确度
    private float maxInaccuracyByHeat = 1.0F;

    // 冷却属性
    private long overheatLocktimeMs = 3000L;
    private long coolingDelayMs = 1000L;
    private float coolingSpeedMultiplier = 1.0F;

    private static final _HeatData PARSER = new _HeatData();
    public static _HeatData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _HeatData fromJsonReader(JsonReader reader) throws IOException {
        _HeatData pojo = new _HeatData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _HeatDataTag.MAX_HEAT, _HeatDataTag.MAX_HEAT_OLD1 -> pojo.maxHeat = JsonUtils.readFloat(reader);
                    case _HeatDataTag.HEAT_PER_SHOT, _HeatDataTag.HEAT_PER_SHOT_OLD1 -> pojo.heatPerShot = JsonUtils.readFloat(reader);

                    case _HeatDataTag.MIN_RPM_BY_HEAT, _HeatDataTag.MIN_RPM_BY_HEAT_OLD1 -> pojo.minRpmByHeat = JsonUtils.readFloat(reader);
                    case _HeatDataTag.MAX_RPM_BY_HEAT, _HeatDataTag.MAX_RPM_BY_HEAT_OLD1 -> pojo.maxRpmByHeat = JsonUtils.readFloat(reader);
                    case _HeatDataTag.MIN_INACCURACY_BY_HEAT, _HeatDataTag.MIN_INACCURACY_BY_HEAT_OLD1 -> pojo.minInaccuracyByHeat = JsonUtils.readFloat(reader);
                    case _HeatDataTag.MAX_INACCURACY_BY_HEAT, _HeatDataTag.MAX_INACCURACY_BY_HEAT_OLD1 -> pojo.maxInaccuracyByHeat = JsonUtils.readFloat(reader);

                    case _HeatDataTag.OVERHEAT_LOCKTIME_MS, _HeatDataTag.OVERHEAT_LOCKTIME_MS_OLD1 -> pojo.overheatLocktimeMs = JsonUtils.readLong(reader);
                    case _HeatDataTag.COOLING_DELAY_MS, _HeatDataTag.COOLING_DELAY_MS_OLD1 -> pojo.coolingDelayMs = JsonUtils.readLong(reader);
                    case _HeatDataTag.COOLING_SPEED_MULTIPLIER, _HeatDataTag.COOLING_SPEED_MULTIPLIER_OLD1 -> pojo.coolingSpeedMultiplier = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _HeatData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _HeatDataTag.MAX_HEAT, maxHeat);
            JsonUtils.writeFloat(writer, _HeatDataTag.HEAT_PER_SHOT, heatPerShot);

            JsonUtils.writeFloat(writer, _HeatDataTag.MIN_RPM_BY_HEAT, minRpmByHeat);
            JsonUtils.writeFloat(writer, _HeatDataTag.MAX_RPM_BY_HEAT, maxRpmByHeat);
            JsonUtils.writeFloat(writer, _HeatDataTag.MIN_INACCURACY_BY_HEAT, minInaccuracyByHeat);
            JsonUtils.writeFloat(writer, _HeatDataTag.MAX_INACCURACY_BY_HEAT, maxInaccuracyByHeat);

            JsonUtils.writeLong(writer, _HeatDataTag.OVERHEAT_LOCKTIME_MS, overheatLocktimeMs);
            JsonUtils.writeLong(writer, _HeatDataTag.COOLING_DELAY_MS, coolingDelayMs);
            JsonUtils.writeFloat(writer, _HeatDataTag.COOLING_SPEED_MULTIPLIER, coolingSpeedMultiplier);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // -------- Getter & Setter --------

    public float getMaxHeat() {
        return maxHeat;
    }
    public float getHeatPerShot() {
        return heatPerShot;
    }
    public float getMinRpmByHeat() {
        return minRpmByHeat;
    }
    public float getMaxRpmByHeat() {
        return maxRpmByHeat;
    }
    public float getMinInaccuracyByHeat() {
        return minInaccuracyByHeat;
    }
    public float getMaxInaccuracyByHeat() {
        return maxInaccuracyByHeat;
    }
    public long getOverheatLocktimeMs() {
        return overheatLocktimeMs;
    }
    public long getCoolingDelayMs() {
        return coolingDelayMs;
    }
    public float getCoolingSpeedMultiplier() {
        return coolingSpeedMultiplier;
    }

    public void setMaxHeat(float maxHeat) {
        this.maxHeat = maxHeat;
    }
    public void setHeatPerShot(float heatPerShot) {
        this.heatPerShot = heatPerShot;
    }
    public void setMinRpmByHeat(float minRpmByHeat) {
        this.minRpmByHeat = minRpmByHeat;
    }
    public void setMaxRpmByHeat(float maxRpmByHeat) {
        this.maxRpmByHeat = maxRpmByHeat;
    }
    public void setMinInaccuracyByHeat(float minInaccuracyByHeat) {
        this.minInaccuracyByHeat = minInaccuracyByHeat;
    }
    public void setMaxInaccuracyByHeat(float maxInaccuracyByHeat) {
        this.maxInaccuracyByHeat = maxInaccuracyByHeat;
    }
    public void setOverheatLocktimeMs(long overheatLocktimeMs) {
        this.overheatLocktimeMs = overheatLocktimeMs;
    }
    public void setCoolingDelayMs(long coolingDelayMs) {
        this.coolingDelayMs = coolingDelayMs;
    }
    public void setCoolingSpeedMultiplier(float coolingSpeedMultiplier) {
        this.coolingSpeedMultiplier = coolingSpeedMultiplier;
    }
}