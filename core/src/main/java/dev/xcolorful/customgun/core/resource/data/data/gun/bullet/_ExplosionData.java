/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.gun.bullet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun.bullet._ExplosionDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ExplosionData extends ResourcePojo<_ExplosionData> {

    // 总开关
    private boolean enableExplode = false;

    // 爆炸属性
    private float explodeDamage = 0.0F;
    private float explodeScale = 0.0F;
    private float maxDelaySeconds = 30.0F;

    // 爆炸规则
    private boolean enableKnockback = false;
    private boolean enableWorldDestruction = false;

    private static final _ExplosionData PARSER = new _ExplosionData();
    public static _ExplosionData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ExplosionData fromJsonReader(JsonReader reader) throws IOException {
        _ExplosionData pojo = new _ExplosionData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ExplosionDataTag.ENABLE_EXPLODE, _ExplosionDataTag.ENABLE_EXPLODE_OLD1 -> pojo.enableExplode = JsonUtils.readBoolean(reader);

                    case _ExplosionDataTag.EXPLODE_DAMAGE, _ExplosionDataTag.EXPLODE_DAMAGE_OLD1 -> pojo.explodeDamage = JsonUtils.readFloat(reader);
                    case _ExplosionDataTag.EXPLODE_SCALE, _ExplosionDataTag.EXPLODE_SCALE_OLD1 -> pojo.explodeScale = JsonUtils.readFloat(reader);
                    case _ExplosionDataTag.MAX_DELAY_SECONDS, _ExplosionDataTag.MAX_DELAY_SECONDS_OLD1 -> pojo.maxDelaySeconds = JsonUtils.readFloat(reader);

                    case _ExplosionDataTag.ENABLE_KNOCKBACK, _ExplosionDataTag.ENABLE_KNOCKBACK_OLD1 -> pojo.enableKnockback = JsonUtils.readBoolean(reader);
                    case _ExplosionDataTag.ENABLE_WORLD_DESTRUCTION, _ExplosionDataTag.ENABLE_WORLD_DESTRUCTION_OLD1 -> pojo.enableWorldDestruction = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ExplosionData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeBoolean(writer, _ExplosionDataTag.ENABLE_EXPLODE, this.enableExplode);

            JsonUtils.writeFloat(writer, _ExplosionDataTag.EXPLODE_DAMAGE, this.explodeDamage);
            JsonUtils.writeFloat(writer, _ExplosionDataTag.EXPLODE_SCALE, this.explodeScale);
            JsonUtils.writeFloat(writer, _ExplosionDataTag.MAX_DELAY_SECONDS, this.maxDelaySeconds);

            JsonUtils.writeBoolean(writer, _ExplosionDataTag.ENABLE_KNOCKBACK, this.enableKnockback);
            JsonUtils.writeBoolean(writer, _ExplosionDataTag.ENABLE_WORLD_DESTRUCTION, this.enableWorldDestruction);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public boolean getEnableExplode() {
        return enableExplode;
    }
    public float getExplodeDamage() {
        return explodeDamage;
    }
    public float getExplodeScale() {
        return explodeScale;
    }
    public float getMaxDelaySeconds() {
        return maxDelaySeconds;
    }
    public boolean getEnableKnockback() {
        return enableKnockback;
    }
    public boolean getEnableWorldDestruction() {
        return enableWorldDestruction;
    }

    public void setEnableExplode(boolean enableExplode) {
        this.enableExplode = enableExplode;
    }
    public void setExplodeDamage(float explodeDamage) {
        this.explodeDamage = explodeDamage;
    }
    public void setExplodeScale(float explodeScale) {
        this.explodeScale = explodeScale;
    }
    public void setMaxDelaySeconds(float maxDelaySeconds) {
        this.maxDelaySeconds = maxDelaySeconds;
    }
    public void setEnableKnockback(boolean enableKnockback) {
        this.enableKnockback = enableKnockback;
    }
    public void setEnableWorldDestruction(boolean enableWorldDestruction) {
        this.enableWorldDestruction = enableWorldDestruction;
    }

    // --------Back compatibility--------
}