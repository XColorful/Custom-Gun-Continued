/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._BurstDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _BurstData extends ResourcePojo<_BurstData> {

    private int bpm = 200;
    private int burstAmount = 3;
    private float shootIntervalSeconds = 1;
    private boolean continuousShoot = false;

    private static final _BurstData PARSER = new _BurstData();
    public static _BurstData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _BurstData fromJsonReader(JsonReader reader) throws IOException {
        _BurstData pojo = new _BurstData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BurstDataTag.BPM -> pojo.bpm = JsonUtils.readInt(reader);
                    case _BurstDataTag.BURST_AMOUNT, _BurstDataTag.BURST_AMOUNT_OLD1 -> pojo.burstAmount = JsonUtils.readInt(reader);
                    case _BurstDataTag.SHOOT_INTERVAL_SECONDS, _BurstDataTag.SHOOT_INTERVAL_SECONDS_OLD1 -> pojo.shootIntervalSeconds = JsonUtils.readFloat(reader);
                    case _BurstDataTag.CONTINUOUS_SHOOT -> pojo.continuousShoot = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _BurstData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeInt(writer, _BurstDataTag.BPM, this.bpm);
            JsonUtils.writeInt(writer, _BurstDataTag.BURST_AMOUNT, this.burstAmount);
            JsonUtils.writeFloat(writer, _BurstDataTag.SHOOT_INTERVAL_SECONDS, this.shootIntervalSeconds);
            JsonUtils.writeBoolean(writer, _BurstDataTag.CONTINUOUS_SHOOT, this.continuousShoot);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public int getBpm() {
        return bpm;
    }
    public int getBurstAmount() {
        return burstAmount;
    }
    public float getShootIntervalSeconds() {
        return shootIntervalSeconds;
    }
    public boolean getContinuousShoot() {
        return continuousShoot;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }
    public void setBurstAmount(int burstAmount) {
        this.burstAmount = burstAmount;
    }
    public void setShootIntervalSeconds(float shootIntervalSeconds) {
        this.shootIntervalSeconds = shootIntervalSeconds;
    }
    public void setContinuousShoot(boolean continuousShoot) {
        this.continuousShoot = continuousShoot;
    }

    // --------Back compatibility--------
}