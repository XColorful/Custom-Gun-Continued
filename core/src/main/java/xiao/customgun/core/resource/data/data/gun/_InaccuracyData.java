/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._InaccuracyDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _InaccuracyData extends ResourcePojo<_InaccuracyData> {

    private float stand = 5.0F;
    private float move = 5.75F;
    private float ride = 5.5F;
    private float sneak = 3.5F;
    private float prone = 2.5F;
    private float aim = 0.15F;
    private float levitate = 6.0F;

    private static final _InaccuracyData PARSER = new _InaccuracyData();
    public static _InaccuracyData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _InaccuracyData fromJsonReader(JsonReader reader) throws IOException {
        _InaccuracyData pojo = new _InaccuracyData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _InaccuracyDataTag.STAND -> pojo.stand = JsonUtils.readFloat(reader);
                    case _InaccuracyDataTag.MOVE -> pojo.move = JsonUtils.readFloat(reader);
                    case _InaccuracyDataTag.RIDE -> pojo.ride = JsonUtils.readFloat(reader);
                    case _InaccuracyDataTag.SNEAK -> pojo.sneak = JsonUtils.readFloat(reader);
                    case _InaccuracyDataTag.PRONE, _InaccuracyDataTag.PRONE_OLD1 -> pojo.prone = JsonUtils.readFloat(reader);
                    case _InaccuracyDataTag.AIM -> pojo.aim = JsonUtils.readFloat(reader);
                    case _InaccuracyDataTag.LEVITATE -> pojo.levitate = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _InaccuracyData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.STAND, this.stand);
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.MOVE, this.move);
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.RIDE, this.ride);
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.SNEAK, this.sneak);
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.PRONE, this.prone);
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.AIM, this.aim);
            JsonUtils.writeFloat(writer, _InaccuracyDataTag.LEVITATE, this.levitate);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getStand() {
        return stand;
    }
    public float getMove() {
        return move;
    }
    public float getRide() {
        return ride;
    }
    public float getSneak() {
        return sneak;
    }
    public float getProne() {
        return prone;
    }
    public float getAim() {
        return aim;
    }
    public float getLevitate() {
        return levitate;
    }

    public void setStand(float stand) {
        this.stand = stand;
    }
    public void setMove(float move) {
        this.move = move;
    }
    public void setRide(float ride) {
        this.ride = ride;
    }
    public void setSneak(float sneak) {
        this.sneak = sneak;
    }
    public void setProne(float prone) {
        this.prone = prone;
    }
    public void setAim(float aim) {
        this.aim = aim;
    }
    public void setLevitate(float levitate) {
        this.levitate = levitate;
    }

    // --------Back compatibility--------
}