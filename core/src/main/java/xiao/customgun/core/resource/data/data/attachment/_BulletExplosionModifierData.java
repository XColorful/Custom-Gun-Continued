/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.data.data.attachment._BulletExplosionModifierDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _BulletExplosionModifierData extends ResourcePojo<_BulletExplosionModifierData> {

    // 总开关
    private boolean enableExplode = false;

    // 爆炸属性
    private @Nullable _SimpleModifierData explodeDamageModifier;
    private @Nullable _SimpleModifierData explodeScaleModifier;
    private @Nullable _SimpleModifierData maxDelaySecondsModifier;

    // 爆炸规则
    private boolean enableKnockback = false;
    private boolean enableWorldDestruction = false;

    private static final _BulletExplosionModifierData PARSER = new _BulletExplosionModifierData();
    public static _BulletExplosionModifierData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _BulletExplosionModifierData fromJsonReader(JsonReader reader) throws IOException {
        _BulletExplosionModifierData pojo = new _BulletExplosionModifierData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BulletExplosionModifierDataTag.ENABLE_EXPLODE, _BulletExplosionModifierDataTag.ENABLE_EXPLODE_OLD1 -> pojo.enableExplode = JsonUtils.readBoolean(reader);

                    case _BulletExplosionModifierDataTag.EXPLODE_DAMAGE, _BulletExplosionModifierDataTag.EXPLODE_DAMAGE_OLD1 -> pojo.explodeDamageModifier = _SimpleModifierData.fromJson(reader);
                    case _BulletExplosionModifierDataTag.EXPLODE_SCALE, _BulletExplosionModifierDataTag.EXPLODE_SCALE_OLD1 -> pojo.explodeScaleModifier = _SimpleModifierData.fromJson(reader);
                    case _BulletExplosionModifierDataTag.MAX_DELAY_SECONDS, _BulletExplosionModifierDataTag.MAX_DELAY_SECONDS_OLD1 -> pojo.maxDelaySecondsModifier = _SimpleModifierData.fromJson(reader);

                    case _BulletExplosionModifierDataTag.ENABLE_KNOCKBACK, _BulletExplosionModifierDataTag.ENABLE_KNOCKBACK_OLD1 -> pojo.enableKnockback = JsonUtils.readBoolean(reader);
                    case _BulletExplosionModifierDataTag.ENABLE_WORLD_DESTRUCTION, _BulletExplosionModifierDataTag.ENABLE_WORLD_DESTRUCTION_OLD1 -> pojo.enableWorldDestruction = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _BulletExplosionModifierData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeBoolean(writer, _BulletExplosionModifierDataTag.ENABLE_EXPLODE, this.enableExplode);

            JsonUtils.write(writer, _BulletExplosionModifierDataTag.EXPLODE_DAMAGE, this.explodeDamageModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, _BulletExplosionModifierDataTag.EXPLODE_SCALE, this.explodeScaleModifier, _SimpleModifierData::toJson);
            JsonUtils.write(writer, _BulletExplosionModifierDataTag.MAX_DELAY_SECONDS, this.maxDelaySecondsModifier, _SimpleModifierData::toJson);

            JsonUtils.writeBoolean(writer, _BulletExplosionModifierDataTag.ENABLE_KNOCKBACK, this.enableKnockback);
            JsonUtils.writeBoolean(writer, _BulletExplosionModifierDataTag.ENABLE_WORLD_DESTRUCTION, this.enableWorldDestruction);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (this.explodeDamageModifier != null) this.explodeDamageModifier.validate();
        if (this.explodeScaleModifier != null) this.explodeScaleModifier.validate();
        if (this.maxDelaySecondsModifier != null) this.maxDelaySecondsModifier.validate();
        boolean v1 = ((this.explodeDamageModifier == null || this.explodeDamageModifier.isValid()) & (this.explodeScaleModifier == null || this.explodeScaleModifier.isValid()) & (this.maxDelaySecondsModifier == null || this.maxDelaySecondsModifier.isValid()));
        if (!(v1)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public boolean getEnableExplode() {
        return enableExplode;
    }
    public @Nullable _SimpleModifierData getExplodeDamageModifier() {
        return explodeDamageModifier;
    }
    public @Nullable _SimpleModifierData getExplodeScaleModifier() {
        return explodeScaleModifier;
    }
    public @Nullable _SimpleModifierData getMaxDelaySecondsModifier() {
        return maxDelaySecondsModifier;
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
    public void setExplodeDamageModifier(_SimpleModifierData explodeDamageModifier) {
        this.explodeDamageModifier = explodeDamageModifier;
    }
    public void setExplodeScaleModifier(_SimpleModifierData explodeScaleModifier) {
        this.explodeScaleModifier = explodeScaleModifier;
    }
    public void setMaxDelaySecondsModifier(_SimpleModifierData maxDelaySecondsModifier) {
        this.maxDelaySecondsModifier = maxDelaySecondsModifier;
    }
    public void setEnableKnockback(boolean enableKnockback) {
        this.enableKnockback = enableKnockback;
    }
    public void setEnableWorldDestruction(boolean enableWorldDestruction) {
        this.enableWorldDestruction = enableWorldDestruction;
    }

    // --------Back compatibility--------

    @Override
    public _BulletExplosionModifierData applyBackCompatibility() {
        if (this.explodeDamageModifier != null) this.explodeDamageModifier.applyBackCompatibility();
        if (this.explodeScaleModifier != null) this.explodeScaleModifier.applyBackCompatibility();
        if (this.maxDelaySecondsModifier != null) this.maxDelaySecondsModifier.applyBackCompatibility();
        return this;
    }
}