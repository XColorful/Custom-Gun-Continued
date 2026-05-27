/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.gun.ChargeType;
import xiao.customgun.core.api.resource.data.data.gun._ChargingDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ChargingData extends ResourcePojo<_ChargingData> {

    private ChargeType chargeType;
    private float maxCharge = 1.0F;
    private float fireThreshold = 0.6F;
    private float recoverByFire = 0.0F;

    // 时间属性
    private float chargePerTick = 0.2F;
    private float recoverPerTick = 0.5F;
    private boolean enableChargeDuringCooldown = true;

    private static final _ChargingData PARSER = new _ChargingData();
    public static _ChargingData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ChargingData fromJsonReader(JsonReader reader) throws IOException {
        _ChargingData pojo = new _ChargingData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ChargingDataTag.CHARGE_TYPE, _ChargingDataTag.CHARGE_TYPE_OLD1 -> pojo.chargeType = JsonUtils.readFromString(reader, ChargeType::fromString);
                    case _ChargingDataTag.MAX_CHARGE -> pojo.maxCharge = JsonUtils.readFloat(reader);
                    case _ChargingDataTag.FIRE_THRESHOLD -> pojo.fireThreshold = JsonUtils.readFloat(reader);
                    case _ChargingDataTag.RECOVER_BY_FIRE, _ChargingDataTag.RECOVER_BY_FIRE_OLD1 -> pojo.recoverByFire = JsonUtils.readFloat(reader);

                    case _ChargingDataTag.CHARGE_PER_TICK, _ChargingDataTag.CHARGE_PER_TICK_OLD1 -> pojo.chargePerTick = JsonUtils.readFloat(reader);
                    case _ChargingDataTag.RECOVER_PER_TICK, _ChargingDataTag.RECOVER_PER_TICK_OLD1 -> pojo.recoverPerTick = JsonUtils.readFloat(reader);
                    case _ChargingDataTag.ENABLE_CHARGE_DURING_COOLDOWN, _ChargingDataTag.ENABLE_CHARGE_DURING_COOLDOWN_OLD1 -> pojo.enableChargeDuringCooldown = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ChargingData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeToString(writer, _ChargingDataTag.CHARGE_TYPE, chargeType);
            JsonUtils.writeFloat(writer, _ChargingDataTag.MAX_CHARGE, maxCharge);
            JsonUtils.writeFloat(writer, _ChargingDataTag.FIRE_THRESHOLD, fireThreshold);
            JsonUtils.writeFloat(writer, _ChargingDataTag.RECOVER_BY_FIRE, recoverByFire);

            JsonUtils.writeFloat(writer, _ChargingDataTag.CHARGE_PER_TICK, chargePerTick);
            JsonUtils.writeFloat(writer, _ChargingDataTag.RECOVER_PER_TICK, recoverPerTick);
            JsonUtils.writeBoolean(writer, _ChargingDataTag.ENABLE_CHARGE_DURING_COOLDOWN, enableChargeDuringCooldown);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        boolean n1 = (this.chargeType == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // -------- Getter & Setter --------

    public ChargeType getChargeType() {
        return chargeType;
    }
    public float getMaxCharge() {
        return maxCharge;
    }
    public float getFireThreshold() {
        return fireThreshold;
    }
    public float getRecoverByFire() {
        return recoverByFire;
    }
    public float getChargePerTick() {
        return chargePerTick;
    }
    public float getRecoverPerTick() {
        return recoverPerTick;
    }
    public boolean getEnableChargeDuringCooldown() {
        return enableChargeDuringCooldown;
    }

    public void setChargeType(ChargeType chargeType) {
        this.chargeType = chargeType;
    }
    public void setMaxCharge(float maxCharge) {
        this.maxCharge = maxCharge;
    }
    public void setFireThreshold(float fireThreshold) {
        this.fireThreshold = fireThreshold;
    }
    public void setRecoverByFire(float recoverByFire) {
        this.recoverByFire = recoverByFire;
    }
    public void setChargePerTick(float chargePerTick) {
        this.chargePerTick = chargePerTick;
    }
    public void setRecoverPerTick(float recoverPerTick) {
        this.recoverPerTick = recoverPerTick;
    }
    public void setEnableChargeDuringCooldown(boolean enableChargeDuringCooldown) {
        this.enableChargeDuringCooldown = enableChargeDuringCooldown;
    }
}