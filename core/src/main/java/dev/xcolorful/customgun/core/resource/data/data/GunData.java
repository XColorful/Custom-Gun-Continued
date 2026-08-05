/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.data.data.GunDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.gun.*;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GunData extends ResourcePojo<GunData> {

    // 枪械属性
    private _BulletData bulletData; // 子弹属性
    private ResourceLocation ammoLocation; // 子弹类型
    private BoltType boltType; // 拉栓类型

    private int rpm = 300; // 射速
    private _InaccuracyData inaccuracyData; // 射击散布
    private _RecoilData recoilData; // 后坐力
    private float proneRecoilMultiplier = 0.5f; // 趴后坐力

    @Deprecated(forRemoval = true) private float weight = 0f; // 基础移速影响
    @Deprecated(forRemoval = true) private _MovementData movementData; // 移速数据

    private _FireSoundData fireSoundData; // 开火声音范围
    private float hurtBobTweakMultiplier = 0.05f; // 被命中者受击晃动

    private _ReloadData reloadData; // 装弹数据

    // 枪械脚本
    private @Nullable ResourceLocation scriptLocation; // 状态机脚本
    private @Nullable Map<String, Object> scriptParam; // 状态机参数

    // 开火模式
    private FireModeType defaultFireModeType;
    private List<FireModeType> fireModeTypes; // 开火模式
    private Map<FireModeType, _FireModeAdjustData> fireModeAdjustData; // 开火模式数据
    private _BurstData burstData; // 开火模式(2/3连发)模式数据

    // 扩展属性
    private _MeleeData meleeData; // 近战 (刺刀/枪托)
    private @Nullable _HeatData heatData; // 过热
    private Map<FireModeType, _ChargingData> chargingData; // 蓄力/延迟扳机

    // 配件
    private List<AttachmentCategory> allowAttachmentTypes; // 配件槽
    private Map<ResourceLocation, AttachmentData> exclusiveAttachments; // (疑似已损坏功能)
    private int defaultMagSize = 30; // 默认弹匣大小
    private int[] extendedMagAmmoSize; // 扩容弹匣大小
    private Map<AttachmentCategory, ResourceLocation> builtinAttachments; // 默认配件外观

    // 举枪动作
    private boolean enableProne = true;
    private boolean enableSlide = true; // 枪挡在视野中间

    // 操作枪械的时长
    private float drawTime = 0.4f;
    private float putAwayTime = 0.4f;
    private float sprintSwitchTime = 0.2f;
    private float aimTime = 0.2f;
    private float boltActionTime = 0f;
    private float boltFeedTime = -1f;

    private static final GunData PARSER = new GunData();
    public static GunData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GunData fromJsonReader(JsonReader reader) throws IOException {
        GunData pojo = new GunData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case GunDataTag.BULLET_DATA, GunDataTag.BULLET_DATA_OLD1 -> pojo.bulletData = JsonUtils.read(reader, _BulletData::fromJson);
                    case GunDataTag.AMMO_LOCATION, GunDataTag.AMMO_LOCATION_OLD1 -> pojo.ammoLocation = JsonUtils.readResourceLocation(reader);
                    case GunDataTag.BOLT_TYPE, GunDataTag.BOLT_TYPE_OLD1 -> pojo.boltType = JsonUtils.readFromString(reader, BoltType::fromString);

                    case GunDataTag.RPM -> pojo.rpm = JsonUtils.readInt(reader);
                    case GunDataTag.INACCURACY_DATA, GunDataTag.INACCURACY_DATA_OLD1 -> pojo.inaccuracyData = JsonUtils.read(reader, _InaccuracyData::fromJson);
                    case GunDataTag.RECOIL_DATA, GunDataTag.RECOIL_DATA_OLD1 -> pojo.recoilData = JsonUtils.read(reader, _RecoilData::fromJson);
                    case GunDataTag.PRONE_RECOIL_MULTIPLIER, GunDataTag.PRONE_RECOIL_MULTIPLIER_OLD1 -> pojo.proneRecoilMultiplier = JsonUtils.readFloat(reader);

                    case GunDataTag.FIRE_SOUND_DATA, GunDataTag.FIRE_SOUND_DATA_OLD1 -> pojo.fireSoundData = JsonUtils.read(reader, _FireSoundData::fromJson);
                    case GunDataTag.HURT_BOB_TWEAK_MULTIPLIER -> pojo.hurtBobTweakMultiplier = JsonUtils.readFloat(reader);

                    case GunDataTag.RELOAD_DATA, GunDataTag.RELOAD_DATA_OLD1 -> pojo.reloadData = JsonUtils.read(reader, _ReloadData::fromJson);

                    case GunDataTag.SCRIPT_LOCATION, GunDataTag.SCRIPT_LOCATION_OLD1 -> pojo.scriptLocation = JsonUtils.readResourceLocation(reader);
                    case GunDataTag.SCRIPT_PARAM -> pojo.scriptParam = JsonUtils.readString2ObjectMap(reader, JsonUtils::readObject);

                    case GunDataTag.DEFAULT_FIRE_MODE_TYPE, GunDataTag.DEFAULT_FIRE_MODE_TYPE_OLD1 -> pojo.defaultFireModeType = JsonUtils.readFromString(reader, FireModeType::fromString);
                    case GunDataTag.FIRE_MODE_TYPE, GunDataTag.FIRE_MODE_TYPE_OLD1 -> pojo.fireModeTypes = JsonUtils.readFromStringList(reader, FireModeType::fromString);
                    case GunDataTag.FIRE_MODE_ADJUST_DATA, GunDataTag.FIRE_MODE_ADJUST_DATA_OLD1 -> pojo.fireModeAdjustData = JsonUtils.readObject2ObjectMap(reader, FireModeType::fromString, _FireModeAdjustData::fromJson);
                    case GunDataTag.BURST_DATA -> pojo.burstData = JsonUtils.read(reader, _BurstData::fromJson);

                    case GunDataTag.MELEE_DATA, GunDataTag.MELEE_DATA_OLD1 -> pojo.meleeData = JsonUtils.read(reader, _MeleeData::fromJson);
                    case GunDataTag.HEAT_DATA, GunDataTag.HEAT_DATA_OLD1 -> pojo.heatData = JsonUtils.read(reader, _HeatData::fromJson);
                    case GunDataTag.CHARGING_DATA, GunDataTag.CHARGING_DATA_OLD1 -> pojo.chargingData = JsonUtils.readObject2ObjectMap(reader, FireModeType::fromString, _ChargingData::fromJson);

                    case GunDataTag.ALLOW_ATTACHMENT_TYPES -> pojo.allowAttachmentTypes = JsonUtils.readFromStringList(reader, AttachmentCategory::fromString);
                    case GunDataTag.EXCLUSIVE_ATTACHMENTS -> pojo.exclusiveAttachments = JsonUtils.readRl2ObjectMap(reader, AttachmentData::fromJson);
                    case GunDataTag.DEFAULT_MAG_SIZE, GunDataTag.DEFAULT_MAG_SIZE_OLD1 -> pojo.defaultMagSize = JsonUtils.readInt(reader);
                    case GunDataTag.EXTENDED_MAG_AMMO_SIZE, GunDataTag.EXTENDED_MAG_AMMO_SIZE_OLD1 -> pojo.extendedMagAmmoSize = JsonUtils.readIntArray(reader);
                    case GunDataTag.BUILTIN_ATTACHMENTS -> pojo.builtinAttachments = JsonUtils.readObject2ObjectMap(reader, AttachmentCategory::fromString, JsonUtils::readResourceLocation);

                    case GunDataTag.ENABLE_PRONE, GunDataTag.ENABLE_PRONE_OLD1 -> pojo.enableProne = JsonUtils.readBoolean(reader);
                    case GunDataTag.ENABLE_SLIDE, GunDataTag.ENABLE_SLIDE_OLD1 -> pojo.enableSlide = JsonUtils.readBoolean(reader);

                    case GunDataTag.DRAW_TIME -> pojo.drawTime = JsonUtils.readFloat(reader);
                    case GunDataTag.PUT_AWAY_TIME -> pojo.putAwayTime = JsonUtils.readFloat(reader);
                    case GunDataTag.SPRINT_SWITCH_TIME, GunDataTag.SPRINT_SWITCH_TIME_OLD1 -> pojo.sprintSwitchTime = JsonUtils.readFloat(reader);
                    case GunDataTag.AIM_TIME -> pojo.aimTime = JsonUtils.readFloat(reader);
                    case GunDataTag.BOLT_ACTION_TIME -> pojo.boltActionTime = JsonUtils.readFloat(reader);
                    case GunDataTag.BOLT_FEED_TIME -> pojo.boltFeedTime = JsonUtils.readFloat(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, GunData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, GunDataTag.BULLET_DATA, bulletData, _BulletData::toJson);
            JsonUtils.writeResourceLocation(writer, GunDataTag.AMMO_LOCATION, ammoLocation);
            JsonUtils.writeToString(writer, GunDataTag.BOLT_TYPE, boltType);

            JsonUtils.writeInt(writer, GunDataTag.RPM, rpm);
            JsonUtils.write(writer, GunDataTag.INACCURACY_DATA, inaccuracyData, _InaccuracyData::toJson);
            JsonUtils.write(writer, GunDataTag.RECOIL_DATA, recoilData, _RecoilData::toJson);
            JsonUtils.writeFloat(writer, GunDataTag.PRONE_RECOIL_MULTIPLIER, proneRecoilMultiplier);

            JsonUtils.write(writer, GunDataTag.FIRE_SOUND_DATA, fireSoundData, _FireSoundData::toJson);
            JsonUtils.writeFloat(writer, GunDataTag.HURT_BOB_TWEAK_MULTIPLIER, hurtBobTweakMultiplier);

            JsonUtils.write(writer, GunDataTag.RELOAD_DATA, reloadData, _ReloadData::toJson);

            JsonUtils.writeResourceLocation(writer, GunDataTag.SCRIPT_LOCATION, scriptLocation);
            JsonUtils.writeString2ObjectMap(writer, GunDataTag.SCRIPT_PARAM, scriptParam, JsonUtils::writeObject);

            JsonUtils.writeToString(writer, GunDataTag.DEFAULT_FIRE_MODE_TYPE, defaultFireModeType);
            JsonUtils.writeToStringList(writer, GunDataTag.FIRE_MODE_TYPE, fireModeTypes);
            JsonUtils.writeObject2ObjectMap(writer, GunDataTag.FIRE_MODE_ADJUST_DATA, fireModeAdjustData, FireModeType::toString, _FireModeAdjustData::toJson);
            JsonUtils.write(writer, GunDataTag.BURST_DATA, burstData, _BurstData::toJson);

            JsonUtils.write(writer, GunDataTag.MELEE_DATA, meleeData, _MeleeData::toJson);
            JsonUtils.write(writer, GunDataTag.HEAT_DATA, heatData, _HeatData::toJson);
            JsonUtils.writeObject2ObjectMap(writer, GunDataTag.CHARGING_DATA, chargingData, FireModeType::toString, _ChargingData::toJson);

            JsonUtils.writeToStringList(writer, GunDataTag.ALLOW_ATTACHMENT_TYPES, allowAttachmentTypes);
            JsonUtils.writeRl2ObjectMap(writer, GunDataTag.EXCLUSIVE_ATTACHMENTS, exclusiveAttachments, AttachmentData::toJson);
            JsonUtils.writeInt(writer, GunDataTag.DEFAULT_MAG_SIZE, defaultMagSize);
            JsonUtils.writeIntArray(writer, GunDataTag.EXTENDED_MAG_AMMO_SIZE, extendedMagAmmoSize);
            JsonUtils.writeObject2ObjectMap(writer, GunDataTag.BUILTIN_ATTACHMENTS, builtinAttachments, AttachmentCategory::toString, JsonUtils::writeResourceLocationValue);

            JsonUtils.writeBoolean(writer, GunDataTag.ENABLE_PRONE, enableProne);
            JsonUtils.writeBoolean(writer, GunDataTag.ENABLE_SLIDE, enableSlide);

            JsonUtils.writeFloat(writer, GunDataTag.DRAW_TIME, drawTime);
            JsonUtils.writeFloat(writer, GunDataTag.PUT_AWAY_TIME, putAwayTime);
            JsonUtils.writeFloat(writer, GunDataTag.SPRINT_SWITCH_TIME, sprintSwitchTime);
            JsonUtils.writeFloat(writer, GunDataTag.AIM_TIME, aimTime);
            JsonUtils.writeFloat(writer, GunDataTag.BOLT_ACTION_TIME, boltActionTime);
            JsonUtils.writeFloat(writer, GunDataTag.BOLT_FEED_TIME, boltFeedTime);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.bulletData == null | this.ammoLocation == null | this.boltType == null | this.inaccuracyData == null);
        boolean n2 = (this.recoilData == null | this.fireSoundData == null | this.reloadData == null | this.defaultFireModeType == null);
        boolean n3 = (this.fireModeTypes == null | this.fireModeAdjustData == null | this.burstData == null | this.meleeData == null);
        boolean n4 = (this.chargingData == null | this.allowAttachmentTypes == null | this.exclusiveAttachments == null | this.extendedMagAmmoSize == null | this.builtinAttachments == null);
        if (n1 | n2 | n3 | n4) {
            this.setValid(false);
            return;
        }
        this.bulletData.validate();
        this.inaccuracyData.validate();
        this.recoilData.validate();
        this.fireSoundData.validate();
        this.reloadData.validate();
        this.burstData.validate();
        this.meleeData.validate();
        if (this.heatData != null) this.heatData.validate();
        boolean v1 = (this.bulletData.isValid() & this.inaccuracyData.isValid() & this.recoilData.isValid());
        boolean v2 = (this.fireSoundData.isValid() & this.reloadData.isValid() & this.burstData.isValid());
        boolean v3 = (this.meleeData.isValid() & (this.heatData == null || this.heatData.isValid()));
        if (!(v1 & v2 & v3)) {
            this.setValid(false);
            return;
        }

        for (_FireModeAdjustData data : this.fireModeAdjustData.values()) {
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }
        for (_ChargingData data : this.chargingData.values()) {
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }
        for (AttachmentData data : this.exclusiveAttachments.values()) {
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public _BulletData getBulletData() {
        return bulletData;
    }
    public ResourceLocation getAmmoLocation() {
        return ammoLocation;
    }
    public BoltType getBoltType() {
        return boltType;
    }
    public int getRpm() {
        return rpm;
    }
    public _InaccuracyData getInaccuracyData() {
        return inaccuracyData;
    }
    public _RecoilData getRecoilData() {
        return recoilData;
    }
    public float getProneRecoilMultiplier() {
        return proneRecoilMultiplier;
    }
    @Deprecated(forRemoval = true) public float getWeight() {
        return weight;
    }
    @Deprecated(forRemoval = true) public _MovementData getMovementData() {
        return movementData;
    }
    public _FireSoundData getFireSoundData() {
        return fireSoundData;
    }
    public float getHurtBobTweakMultiplier() {
        return hurtBobTweakMultiplier;
    }
    public _ReloadData getReloadData() {
        return reloadData;
    }
    public @Nullable ResourceLocation getScriptLocation() {
        return scriptLocation;
    }
    public @Nullable Map<String, Object> getScriptParam() {
        return scriptParam;
    }
    public FireModeType getDefaultFireModeType() {
        return defaultFireModeType;
    }
    public List<FireModeType> getFireModeTypes() {
        return fireModeTypes;
    }
    public Map<FireModeType, _FireModeAdjustData> getFireModeAdjustData() {
        return fireModeAdjustData;
    }
    public _BurstData getBurstData() {
        return burstData;
    }
    public _MeleeData getMeleeData() {
        return meleeData;
    }
    public @Nullable _HeatData getHeatData() {
        return heatData;
    }
    public Map<FireModeType, _ChargingData> getChargingData() {
        return chargingData;
    }
    public List<AttachmentCategory> getAllowAttachmentTypes() {
        return allowAttachmentTypes;
    }
    public Map<ResourceLocation, AttachmentData> getExclusiveAttachments() {
        return exclusiveAttachments;
    }
    public int getDefaultMagSize() {
        return defaultMagSize;
    }
    public int[] getExtendedMagAmmoSize() {
        return extendedMagAmmoSize;
    }
    public Map<AttachmentCategory, ResourceLocation> getBuiltinAttachments() {
        return builtinAttachments;
    }
    public boolean getEnableProne() {
        return enableProne;
    }
    public boolean getEnableSlide() {
        return enableSlide;
    }
    public float getDrawTime() {
        return drawTime;
    }
    public float getPutAwayTime() {
        return putAwayTime;
    }
    public float getSprintSwitchTime() {
        return sprintSwitchTime;
    }
    public float getAimTime() {
        return aimTime;
    }
    public float getBoltActionTime() {
        return boltActionTime;
    }
    public float getBoltFeedTime() {
        return boltFeedTime;
    }

    public void setBulletData(_BulletData bulletData) {
        this.bulletData = bulletData;
    }
    public void setAmmoLocation(ResourceLocation ammoLocation) {
        this.ammoLocation = ammoLocation;
    }
    public void setBoltType(BoltType boltType) {
        this.boltType = boltType;
    }
    public void setRpm(int rpm) {
        this.rpm = rpm;
    }
    public void setInaccuracyData(_InaccuracyData inaccuracyData) {
        this.inaccuracyData = inaccuracyData;
    }
    public void setRecoilData(_RecoilData recoilData) {
        this.recoilData = recoilData;
    }
    public void setProneRecoilMultiplier(float proneRecoilMultiplier) {
        this.proneRecoilMultiplier = proneRecoilMultiplier;
    }
    @Deprecated(forRemoval = true) public void setWeight(float weight) {
        this.weight = weight;
    }
    @Deprecated(forRemoval = true) public void setMovementData(_MovementData movementData) {
        this.movementData = movementData;
    }
    public void setFireSoundData(_FireSoundData fireSoundData) {
        this.fireSoundData = fireSoundData;
    }
    public void setHurtBobTweakMultiplier(float hurtBobTweakMultiplier) {
        this.hurtBobTweakMultiplier = hurtBobTweakMultiplier;
    }
    public void setReloadData(_ReloadData reloadData) {
        this.reloadData = reloadData;
    }
    public void setScriptLocation(ResourceLocation scriptLocation) {
        this.scriptLocation = scriptLocation;
    }
    public void setScriptParam(Map<String, Object> scriptParam) {
        this.scriptParam = scriptParam;
    }
    public void setDefaultFireModeType(FireModeType defaultFireModeType) {
        this.defaultFireModeType = defaultFireModeType;
    }
    public void setFireModeTypes(List<FireModeType> fireModeTypes) {
        this.fireModeTypes = fireModeTypes;
    }
    public void setFireModeAdjustData(Map<FireModeType, _FireModeAdjustData> fireModeAdjustData) {
        this.fireModeAdjustData = fireModeAdjustData;
    }
    public void setBurstData(_BurstData burstData) {
        this.burstData = burstData;
    }
    public void setMeleeData(_MeleeData meleeData) {
        this.meleeData = meleeData;
    }
    public void setHeatData(_HeatData heatData) {
        this.heatData = heatData;
    }
    public void setChargingData(Map<FireModeType, _ChargingData> chargingData) {
        this.chargingData = chargingData;
    }
    public void setAllowAttachmentTypes(List<AttachmentCategory> allowAttachmentTypes) {
        this.allowAttachmentTypes = allowAttachmentTypes;
    }
    public void setExclusiveAttachments(Map<ResourceLocation, AttachmentData> exclusiveAttachments) {
        this.exclusiveAttachments = exclusiveAttachments;
    }
    public void setDefaultMagSize(int defaultMagSize) {
        this.defaultMagSize = defaultMagSize;
    }
    public void setExtendedMagAmmoSize(int[] extendedMagAmmoSize) {
        this.extendedMagAmmoSize = extendedMagAmmoSize;
    }
    public void setBuiltinAttachments(Map<AttachmentCategory, ResourceLocation> builtinAttachments) {
        this.builtinAttachments = builtinAttachments;
    }
    public void setEnableProne(boolean enableProne) {
        this.enableProne = enableProne;
    }
    public void setEnableSlide(boolean enableSlide) {
        this.enableSlide = enableSlide;
    }
    public void setDrawTime(float drawTime) {
        this.drawTime = drawTime;
    }
    public void setPutAwayTime(float putAwayTime) {
        this.putAwayTime = putAwayTime;
    }
    public void setSprintSwitchTime(float sprintSwitchTime) {
        this.sprintSwitchTime = sprintSwitchTime;
    }
    public void setAimTime(float aimTime) {
        this.aimTime = aimTime;
    }
    public void setBoltActionTime(float boltActionTime) {
        this.boltActionTime = boltActionTime;
    }
    public void setBoltFeedTime(float boltFeedTime) {
        this.boltFeedTime = boltFeedTime;
    }

    // --------Back compatibility--------

    @Override
    public GunData applyBackCompatibility() {
        this.bulletData = this.bulletData == null ? new _BulletData().applyBackCompatibility() : this.bulletData.applyBackCompatibility();
        this.ammoLocation = this.ammoLocation == null ? ResourceTag.NULL_LOCATION : this.ammoLocation;
        this.inaccuracyData = this.inaccuracyData == null ? new _InaccuracyData().applyBackCompatibility() : this.inaccuracyData.applyBackCompatibility();
        this.recoilData = this.recoilData == null ? new _RecoilData().applyBackCompatibility() : this.recoilData.applyBackCompatibility();
        this.fireSoundData = this.fireSoundData == null ? new _FireSoundData().applyBackCompatibility() : this.fireSoundData.applyBackCompatibility();
        this.reloadData = this.reloadData == null ? new _ReloadData().applyBackCompatibility() : this.reloadData.applyBackCompatibility();

        this.defaultFireModeType = this.defaultFireModeType == null ? FireModeType.DEFAULT : this.defaultFireModeType;
        this.fireModeTypes = this.fireModeTypes == null ? new ArrayList<>() : this.fireModeTypes;
        if (this.fireModeAdjustData == null) this.fireModeAdjustData = new HashMap<>();
        else this.fireModeAdjustData.values().forEach(_FireModeAdjustData::applyBackCompatibility);
        this.burstData = this.burstData == null ? new _BurstData().applyBackCompatibility() : this.burstData.applyBackCompatibility();

        this.meleeData = this.meleeData == null ? new _MeleeData().applyBackCompatibility() : this.meleeData.applyBackCompatibility();
        this.heatData = this.heatData == null ? new _HeatData().applyBackCompatibility() : this.heatData.applyBackCompatibility();
        if (this.chargingData == null) this.chargingData = new HashMap<>();
        else this.chargingData.values().forEach(_ChargingData::applyBackCompatibility);

        this.allowAttachmentTypes = this.allowAttachmentTypes == null ? new ArrayList<>() : this.allowAttachmentTypes;
        if (this.exclusiveAttachments == null) this.exclusiveAttachments = new HashMap<>();
        else this.exclusiveAttachments.values().forEach(AttachmentData::applyBackCompatibility);
        this.extendedMagAmmoSize = this.extendedMagAmmoSize == null ? new int[0] : this.extendedMagAmmoSize;
        this.builtinAttachments = this.builtinAttachments == null ? new HashMap<>() : this.builtinAttachments;
        return this;
    }
}