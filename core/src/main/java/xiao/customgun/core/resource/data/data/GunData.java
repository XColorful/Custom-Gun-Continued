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
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.resource.data.data.GunDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.*;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class GunData extends ResourcePojo<GunData> {

    // 枪械属性
    private _BulletData bulletData; // 子弹属性
    private ResourceLocation ammoType; // 子弹类型
    private BoltType boltType; // 拉栓类型

    private int rpm = 300; // 射速
    private _InaccuracyData inaccuracyData; // 射击散布
    private _RecoilData recoilData; // 后坐力
    private float crawlRecoilMultiplier = 0.5f; // 蹲后坐力

    private float weight = 0f; // 基础移速影响
    private _MovementData movementData; // 移速数据

    private _FireSoundData fireSoundData; // 开火声音范围
    private float hurtBobTweakMultiplier = 0.05f; // 被命中者受击晃动

    private _ReloadData reloadData; // 装弹数据

    // 枪械脚本
    private ResourceLocation scriptType; // 状态机脚本
    private Map<String, Object> scriptParam; // 状态机参数

    // 开火模式
    private FireModeType defaultFireModeType;
    private List<FireModeType> fireModeTypes; // 开火模式
    private Map<FireModeType, _FireModeAdjustData> fireModeAdjustData; // 开火模式数据
    private _BurstData burstData; // 开火模式(2/3连发)模式数据

    // 扩展属性
    private _MeleeData meleeData; // 近战 (刺刀/枪托)
    private _HeatData heatData; // 过热
    private Map<FireModeType, _ChargingData> chargingData; // 蓄力/延迟扳机

    // 配件
    private List<AttachmentCategory> allowAttachmentTypes; // 配件槽
    private Map<ResourceLocation, AttachmentData> exclusiveAttachments; // (疑似已损坏功能)
    private int defaultMagSize = 30; // 默认弹匣大小
    private int[] extendedMagAmmoSize; // 扩容弹匣大小
    private Map<AttachmentCategory, ResourceLocation> builtinAttachments; // 默认配件外观

    // 举枪动作
    private boolean enableCrawl = true;
    private boolean enableSlide = true; // 枪挡在视野中间

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
                    case GunDataTag.BULLET_DATA, GunDataTag.BULLET_DATA_OLD1 -> pojo.bulletData = JsonUtils.read(reader, _BulletData::fromJson);
                    case GunDataTag.AMMO_TYPE, GunDataTag.AMMO_TYPE_OLD1 -> pojo.ammoType = JsonUtils.readResourceLocation(reader);
                    case GunDataTag.BOLT_TYPE, GunDataTag.BOLT_TYPE_OLD1 -> pojo.boltType = JsonUtils.readFromString(reader, BoltType::fromString);

                    case GunDataTag.RPM -> pojo.rpm = JsonUtils.readInt(reader);
                    case GunDataTag.INACCURACY_DATA, GunDataTag.INACCURACY_DATA_OLD1 -> pojo.inaccuracyData = JsonUtils.read(reader, _InaccuracyData::fromJson);
                    case GunDataTag.RECOIL_DATA, GunDataTag.RECOIL_DATA_OLD1 -> pojo.recoilData = JsonUtils.read(reader, _RecoilData::fromJson);
                    case GunDataTag.CRAWL_RECOIL_MULTIPLIER -> pojo.crawlRecoilMultiplier = JsonUtils.readFloat(reader);

                    case GunDataTag.WEIGHT -> pojo.weight = JsonUtils.readFloat(reader);
                    case GunDataTag.MOVEMENT_DATA, GunDataTag.MOVEMENT_DATA_OLD1 -> pojo.movementData = JsonUtils.read(reader, _MovementData::fromJson);

                    case GunDataTag.FIRE_SOUND_DATA, GunDataTag.FIRE_SOUND_DATA_OLD1 -> pojo.fireSoundData = JsonUtils.read(reader, _FireSoundData::fromJson);
                    case GunDataTag.HURT_BOB_TWEAK_MULTIPLIER -> pojo.hurtBobTweakMultiplier = JsonUtils.readFloat(reader);

                    case GunDataTag.RELOAD_DATA, GunDataTag.RELOAD_DATA_OLD1 -> pojo.reloadData = JsonUtils.read(reader, _ReloadData::fromJson);

                    case GunDataTag.SCRIPT_TYPE, GunDataTag.SCRIPT_TYPE_OLD1 -> pojo.scriptType = JsonUtils.readResourceLocation(reader);
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

                    case GunDataTag.ENABLE_CRAWL, GunDataTag.ENABLE_CRAWL_OLD1 -> pojo.enableCrawl = JsonUtils.readBoolean(reader);
                    case GunDataTag.ENABLE_SLIDE, GunDataTag.ENABLE_SLIDE_OLD1 -> pojo.enableSlide = JsonUtils.readBoolean(reader);

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
            JsonUtils.writeToString(writer, GunDataTag.BOLT_TYPE, boltType);

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
            JsonUtils.writeObject2ObjectMap(writer, GunDataTag.BUILTIN_ATTACHMENTS, builtinAttachments, AttachmentCategory::toString, (w, rl) -> w.value(rl.toString()));

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
    public _HeatData getHeatData() {
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