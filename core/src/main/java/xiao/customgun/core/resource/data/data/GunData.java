/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.data.data.GunDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.*;
import xiao.customgun.core.resource.data.index.attachment._AttachmentTypeData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GunData extends ResourcePojo<GunData> {

    // 枪械属性
    private _BulletData bulletData;
    private ResourceLocation ammoType;
    private _BoltTypeData boltType;

    private int rpm = 300;
    private _InaccuracyData inaccuracyData;
    private _RecoilData recoilData;
    private float crawlRecoilMultiplier = 0.5f;

    private float weight = 0f;
    private _MovementData movementData;

    private _FireSoundData fireSoundData;
    private float hurtBobTweakMultiplier = 0.05f;

    private _ReloadData reloadData;

    // 枪械脚本
    private ResourceLocation scriptType;
    private Map<String, Object> scriptParam;

    // 开火模式
    private _FireModeTypeData defaultFireModeType;
    private List<_FireModeTypeData> fireModeTypes;
    private _FireModeData fireModeData;
    private _BurstData burstData;

    // 扩展属性
    private _MeleeData meleeData;
    private _HeatData heatData;
    private _ChargingData chargingData;

    // 配件
    private List<_AttachmentTypeData> allowAttachmentTypes;
    private Map<ResourceLocation, AttachmentData> exclusiveAttachments;
    private int defaultMagSize = 30;
    private int[] extendedMagAmmoSize;
    private _BuiltinAttachmentData builtinAttachments;

    // 举枪动作
    private boolean enableCrawl = true;
    private boolean enableSlide = true;

    // 操作枪械的时长
    private float drawTime = 0.4f;
    private float putAwayTime = 0.4f;
    private float sprintTime = 0.2f;
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
                    case GunDataTag.BULLET_DATA -> pojo.bulletData = JsonUtils.read(reader, _BulletData::fromJson);
                    case GunDataTag.AMMO_TYPE -> pojo.ammoType = JsonUtils.readResourceLocation(reader);
                    case GunDataTag.BOLT_TYPE -> pojo.boltType = JsonUtils.read(reader, _BoltTypeData::fromJson);

                    case GunDataTag.RPM -> pojo.rpm = JsonUtils.readInt(reader);
                    case GunDataTag.INACCURACY_DATA -> pojo.inaccuracyData = JsonUtils.read(reader, _InaccuracyData::fromJson);
                    case GunDataTag.RECOIL_DATA -> pojo.recoilData = JsonUtils.read(reader, _RecoilData::fromJson);
                    case GunDataTag.CRAWL_RECOIL_MULTIPLIER -> pojo.crawlRecoilMultiplier = JsonUtils.readFloat(reader);

                    case GunDataTag.WEIGHT -> pojo.weight = JsonUtils.readFloat(reader);
                    case GunDataTag.MOVEMENT_DATA -> pojo.movementData = JsonUtils.read(reader, _MovementData::fromJson);

                    case GunDataTag.FIRE_SOUND_DATA -> pojo.fireSoundData = JsonUtils.read(reader, _FireSoundData::fromJson);
                    case GunDataTag.HURT_BOB_TWEAK_MULTIPLIER -> pojo.hurtBobTweakMultiplier = JsonUtils.readFloat(reader);

                    case GunDataTag.RELOAD_DATA -> pojo.reloadData = JsonUtils.read(reader, _ReloadData::fromJson);

                    case GunDataTag.SCRIPT_TYPE -> pojo.scriptType = JsonUtils.readResourceLocation(reader);
                    case GunDataTag.SCRIPT_PARAM -> pojo.scriptParam = JsonUtils.readString2ObjectMap(reader, JsonUtils::readObject);

                    case GunDataTag.FIRE_MODE_TYPE -> pojo.fireModeTypes = JsonUtils.readList(reader, _FireModeTypeData::fromJson);
                    case GunDataTag.FIRE_MODE_DATA -> pojo.fireModeData = JsonUtils.read(reader, _FireModeData::fromJson);
                    case GunDataTag.BURST_DATA -> pojo.burstData = JsonUtils.read(reader, _BurstData::fromJson);

                    case GunDataTag.MELEE_DATA -> pojo.meleeData = JsonUtils.read(reader, _MeleeData::fromJson);
                    case GunDataTag.HEAT_DATA -> pojo.heatData = JsonUtils.read(reader, _HeatData::fromJson);
                    case GunDataTag.CHARGING_DATA -> pojo.chargingData = JsonUtils.read(reader, _ChargingData::fromJson);

                    case GunDataTag.ALLOW_ATTACHMENT_TYPES -> pojo.allowAttachmentTypes = JsonUtils.readList(reader, _AttachmentTypeData::fromJson);
                    case GunDataTag.EXCLUSIVE_ATTACHMENTS -> pojo.exclusiveAttachments = JsonUtils.readRl2ObjectMap(reader, AttachmentData::fromJson);
                    case GunDataTag.DEFAULT_MAG_SIZE -> pojo.defaultMagSize = JsonUtils.readInt(reader);
                    case GunDataTag.EXTENDED_MAG_AMMO_SIZE -> pojo.extendedMagAmmoSize = JsonUtils.readIntArray(reader);
                    case GunDataTag.BUILTIN_ATTACHMENTS -> pojo.builtinAttachments = JsonUtils.read(reader, _BuiltinAttachmentData::fromJson);

                    case GunDataTag.ENABLE_CRAWL -> pojo.enableCrawl = JsonUtils.readBoolean(reader);
                    case GunDataTag.ENABLE_SLIDE -> pojo.enableSlide = JsonUtils.readBoolean(reader);

                    case GunDataTag.DRAW_TIME -> pojo.drawTime = JsonUtils.readFloat(reader);
                    case GunDataTag.PUT_AWAY_TIME -> pojo.putAwayTime = JsonUtils.readFloat(reader);
                    case GunDataTag.SPRINT_TIME -> pojo.sprintTime = JsonUtils.readFloat(reader);
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
            JsonUtils.writeResourceLocation(writer, GunDataTag.AMMO_TYPE, ammoType);
            JsonUtils.write(writer, GunDataTag.BOLT_TYPE, boltType, _BoltTypeData::toJson);

            JsonUtils.writeInt(writer, GunDataTag.RPM, rpm);
            JsonUtils.write(writer, GunDataTag.INACCURACY_DATA, inaccuracyData, _InaccuracyData::toJson);
            JsonUtils.write(writer, GunDataTag.RECOIL_DATA, recoilData, _RecoilData::toJson);
            JsonUtils.writeFloat(writer, GunDataTag.CRAWL_RECOIL_MULTIPLIER, crawlRecoilMultiplier);

            JsonUtils.writeFloat(writer, GunDataTag.WEIGHT, weight);
            JsonUtils.write(writer, GunDataTag.MOVEMENT_DATA, movementData, _MovementData::toJson);

            JsonUtils.write(writer, GunDataTag.FIRE_SOUND_DATA, fireSoundData, _FireSoundData::toJson);
            JsonUtils.writeFloat(writer, GunDataTag.HURT_BOB_TWEAK_MULTIPLIER, hurtBobTweakMultiplier);

            JsonUtils.write(writer, GunDataTag.RELOAD_DATA, reloadData, _ReloadData::toJson);

            JsonUtils.writeResourceLocation(writer, GunDataTag.SCRIPT_TYPE, scriptType);
            JsonUtils.writeString2ObjectMap(writer, GunDataTag.SCRIPT_PARAM, scriptParam, JsonUtils::writeObject);

            JsonUtils.writeList(writer, GunDataTag.FIRE_MODE_TYPE, fireModeTypes, _FireModeTypeData::toJson);
            JsonUtils.write(writer, GunDataTag.FIRE_MODE_DATA, fireModeData, _FireModeData::toJson);
            JsonUtils.write(writer, GunDataTag.BURST_DATA, burstData, _BurstData::toJson);

            JsonUtils.write(writer, GunDataTag.MELEE_DATA, meleeData, _MeleeData::toJson);
            JsonUtils.write(writer, GunDataTag.HEAT_DATA, heatData, _HeatData::toJson);
            JsonUtils.write(writer, GunDataTag.CHARGING_DATA, chargingData, _ChargingData::toJson);

            JsonUtils.writeList(writer, GunDataTag.ALLOW_ATTACHMENT_TYPES, allowAttachmentTypes, _AttachmentTypeData::toJson);
            JsonUtils.writeRl2ObjectMap(writer, GunDataTag.EXCLUSIVE_ATTACHMENTS, exclusiveAttachments, AttachmentData::toJson);
            JsonUtils.writeInt(writer, GunDataTag.DEFAULT_MAG_SIZE, defaultMagSize);
            JsonUtils.writeIntArray(writer, GunDataTag.EXTENDED_MAG_AMMO_SIZE, extendedMagAmmoSize);
            JsonUtils.write(writer, GunDataTag.BUILTIN_ATTACHMENTS, builtinAttachments, _BuiltinAttachmentData::toJson);

            JsonUtils.writeBoolean(writer, GunDataTag.ENABLE_CRAWL, enableCrawl);
            JsonUtils.writeBoolean(writer, GunDataTag.ENABLE_SLIDE, enableSlide);

            JsonUtils.writeFloat(writer, GunDataTag.DRAW_TIME, drawTime);
            JsonUtils.writeFloat(writer, GunDataTag.PUT_AWAY_TIME, putAwayTime);
            JsonUtils.writeFloat(writer, GunDataTag.SPRINT_TIME, sprintTime);
            JsonUtils.writeFloat(writer, GunDataTag.AIM_TIME, aimTime);
            JsonUtils.writeFloat(writer, GunDataTag.BOLT_ACTION_TIME, boltActionTime);
            JsonUtils.writeFloat(writer, GunDataTag.BOLT_FEED_TIME, boltFeedTime);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public _BulletData getBulletData() {
        return bulletData;
    }
    public ResourceLocation getAmmoType() {
        return ammoType;
    }
    public _BoltTypeData getBoltType() {
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
    public float getCrawlRecoilMultiplier() {
        return crawlRecoilMultiplier;
    }
    public float getWeight() {
        return weight;
    }
    public _MovementData getMovementData() {
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
    public ResourceLocation getScriptType() {
        return scriptType;
    }
    public Map<String, Object> getScriptParam() {
        return scriptParam;
    }
    public _FireModeTypeData getDefaultFireModeType() {
        return defaultFireModeType;
    }
    public List<_FireModeTypeData> getFireModeTypes() {
        return fireModeTypes;
    }
    public _FireModeData getFireModeData() {
        return fireModeData;
    }
    public _BurstData getBurstData() {
        return burstData;
    }
    public _MeleeData getMeleeData() {
        return meleeData;
    }
    public _HeatData getHeatData() {
        return heatData;
    }
    public _ChargingData getChargingData() {
        return chargingData;
    }
    public List<_AttachmentTypeData> getAllowAttachmentTypes() {
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
    public _BuiltinAttachmentData getBuiltinAttachments() {
        return builtinAttachments;
    }
    public boolean getEnableCrawl() {
        return enableCrawl;
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
    public float getSprintTime() {
        return sprintTime;
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
    public void setAmmoType(ResourceLocation ammoType) {
        this.ammoType = ammoType;
    }
    public void setBoltType(_BoltTypeData boltType) {
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
    public void setCrawlRecoilMultiplier(float crawlRecoilMultiplier) {
        this.crawlRecoilMultiplier = crawlRecoilMultiplier;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setMovementData(_MovementData movementData) {
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
    public void setScriptType(ResourceLocation scriptType) {
        this.scriptType = scriptType;
    }
    public void setScriptParam(Map<String, Object> scriptParam) {
        this.scriptParam = scriptParam;
    }
    public void setDefaultFireModeType(_FireModeTypeData defaultFireModeType) {
        this.defaultFireModeType = defaultFireModeType;
    }
    public void setFireModeTypes(List<_FireModeTypeData> fireModeTypes) {
        this.fireModeTypes = fireModeTypes;
    }
    public void setFireModeData(_FireModeData fireModeData) {
        this.fireModeData = fireModeData;
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
    public void setChargingData(_ChargingData chargingData) {
        this.chargingData = chargingData;
    }
    public void setAllowAttachmentTypes(List<_AttachmentTypeData> allowAttachmentTypes) {
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
    public void setBuiltinAttachments(_BuiltinAttachmentData builtinAttachments) {
        this.builtinAttachments = builtinAttachments;
    }
    public void setEnableCrawl(boolean enableCrawl) {
        this.enableCrawl = enableCrawl;
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
    public void setSprintTime(float sprintTime) {
        this.sprintTime = sprintTime;
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
}