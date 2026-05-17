/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.client.api.item.gun.DamageDisplayType;
import xiao.customgun.client.api.item.gun.ThirdPersonAnimationType;
import xiao.customgun.client.api.model.gun.GunModelType;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.assets.display.gun.*;
import xiao.customgun.core.api.item.gun.AmmoCountType;
import xiao.customgun.core.api.resource.assets.display.GunDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class GunDisplay extends _AssetsDisplay<GunDisplay> {

    // 材质
    private ResourceLocation hudTextureLocation;
    private ResourceLocation hudEmptyTextureLocation;

    // 模型
    private GunModelType gunModelType;
    private _LodDisplay lodDisplay;
    private boolean enableTransparency = false;

    // 显示
    private float ironZoomScale = 1.2f;
    private float ironViewFov = 70f;
    private boolean enableCrosshair = false;
    private _MuzzleFlashDisplay muzzleFlashDisplay;
    private _LaserDisplay laserDisplay;
    private Map<String, _SurroundDisplay> surroundDisplayByHotbar;
    private _SurroundDisplay surroundDisplayByOffhand;
    private DamageDisplayType damageDisplayType;
    private AmmoCountType ammoCountType;
    private _AmmoDisplayOverride ammoDisplayOverride;

    // 动画
    private ResourceLocation gunAnimationLocation;
    private ResourceLocation scriptLocation;
    private Map<String, Object> scriptParam;
    private _ShellEjectionParam shellEjectionParam;
    private ThirdPersonAnimationType thirdPersonAnimationType;
    private ResourceLocation playerAnimatorLocation;
    private boolean playerAnimatorFixedHand = false;
    private Map<GunSoundType, ResourceLocation> gunSounds;
    private List<String> preloadSoundLocation;

    private _ControllableData controllableData;

    private static final GunDisplay PARSER = new GunDisplay();
    public static GunDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GunDisplay fromJsonReader(JsonReader reader) throws IOException {
        GunDisplay pojo = new GunDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case GunDisplayTag.MODEL_LOCATION, GunDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case GunDisplayTag.TEXTURE_LOCATION, GunDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    case GunDisplayTag.SLOT_TEXTURE_LOCATION, GunDisplayTag.SLOT_TEXTURE_LOCATION_OLD1 -> pojo.setSlotTextureLocation(JsonUtils.readResourceLocation(reader));

                    case GunDisplayTag.HUD_TEXTURE_LOCATION, GunDisplayTag.HUD_TEXTURE_LOCATION_OLD1 -> pojo.hudTextureLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.HUD_EMPTY_TEXTURE_LOCATION, GunDisplayTag.HUD_EMPTY_TEXTURE_LOCATION_OLD1 -> pojo.hudEmptyTextureLocation = JsonUtils.readResourceLocation(reader);

                    case GunDisplayTag.GUN_MODEL_TYPE, GunDisplayTag.GUN_MODEL_TYPE_OLD1 -> pojo.gunModelType = JsonUtils.readFromString(reader, GunModelType::fromString);
                    case GunDisplayTag.LOD_DISPLAY, GunDisplayTag.LOD_DISPLAY_OLD1 -> pojo.lodDisplay = _LodDisplay.fromJson(reader);
                    case GunDisplayTag.ENABLE_TRANSPARENCY -> pojo.enableTransparency = JsonUtils.readBoolean(reader);

                    case GunDisplayTag.IRON_ZOOM_SCALE, GunDisplayTag.IRON_ZOOM_SCALE_OLD1 -> pojo.ironZoomScale = JsonUtils.readFloat(reader);
                    case GunDisplayTag.IRON_VIEW_FOV, GunDisplayTag.IRON_VIEW_FOV_OLD1 -> pojo.ironViewFov = JsonUtils.readFloat(reader);
                    case GunDisplayTag.ENABLE_CROSSHAIR, GunDisplayTag.ENABLE_CROSSHAIR_OLD1 -> pojo.enableCrosshair = JsonUtils.readBoolean(reader);
                    case GunDisplayTag.MUZZLE_FLASH_DISPLAY, GunDisplayTag.MUZZLE_FLASH_DISPLAY_OLD1 -> pojo.muzzleFlashDisplay = _MuzzleFlashDisplay.fromJson(reader);
                    case GunDisplayTag.LASER_DISPLAY, GunDisplayTag.LASER_DISPLAY_OLD1 -> pojo.laserDisplay = _LaserDisplay.fromJson(reader);
                    case GunDisplayTag.SURROUND_DISPLAY_BY_HOTBAR, GunDisplayTag.SURROUND_DISPLAY_BY_HOTBAR_OLD1 -> pojo.surroundDisplayByHotbar = JsonUtils.readString2ObjectMap(reader, _SurroundDisplay::fromJson);
                    case GunDisplayTag.SURROUND_DISPLAY_BY_OFFHAND, GunDisplayTag.SURROUND_DISPLAY_BY_OFFHAND_OLD1 -> pojo.surroundDisplayByOffhand = _SurroundDisplay.fromJson(reader);
                    case GunDisplayTag.DAMAGE_DISPLAY_TYPE, GunDisplayTag.DAMAGE_DISPLAY_TYPE_OLD1 -> pojo.damageDisplayType = JsonUtils.readFromString(reader, DamageDisplayType::fromString);
                    case GunDisplayTag.AMMO_COUNT_TYPE, GunDisplayTag.AMMO_COUNT_TYPE_OLD1 -> pojo.ammoCountType = JsonUtils.readFromString(reader, AmmoCountType::fromString);
                    case GunDisplayTag.AMMO_DISPLAY_OVERRIDE, GunDisplayTag.AMMO_DISPLAY_OVERRIDE_OLD1 -> pojo.ammoDisplayOverride = _AmmoDisplayOverride.fromJson(reader);

                    case GunDisplayTag.GUN_ANIMATION_LOCATION, GunDisplayTag.GUN_ANIMATION_LOCATION_OLD1 -> pojo.gunAnimationLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.SCRIPT_LOCATION, GunDisplayTag.SCRIPT_LOCATION_OLD1 -> pojo.scriptLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.SCRIPT_PARAM, GunDisplayTag.SCRIPT_PARAM_OLD1 -> pojo.scriptParam = JsonUtils.readString2ObjectMap(reader, JsonUtils::readObject);
                    case GunDisplayTag.SHELL_EJECTION_PARAM, GunDisplayTag.SHELL_EJECTION_PARAM_OLD1 -> pojo.shellEjectionParam = _ShellEjectionParam.fromJson(reader);
                    case GunDisplayTag.THIRD_PERSON_ANIMATION_TYPE, GunDisplayTag.THIRD_PERSON_ANIMATION_TYPE_OLD1 -> pojo.thirdPersonAnimationType = JsonUtils.readFromString(reader, ThirdPersonAnimationType::fromString);
                    case GunDisplayTag.PLAYER_ANIMATOR_LOCATION, GunDisplayTag.PLAYER_ANIMATOR_LOCATION_OLD1 -> pojo.playerAnimatorLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.PLAYER_ANIMATOR_FIXED_HAND, GunDisplayTag.PLAYER_ANIMATOR_FIXED_HAND_OLD1 -> pojo.playerAnimatorFixedHand = JsonUtils.readBoolean(reader);
                    case GunDisplayTag.GUN_SOUNDS, GunDisplayTag.GUN_SOUNDS_OLD1 -> pojo.gunSounds = JsonUtils.readObject2ObjectMap(reader, GunSoundType::fromString, JsonUtils::readResourceLocation);
                    case GunDisplayTag.PRELOAD_SOUND_LOCATION, GunDisplayTag.PRELOAD_SOUND_LOCATION_OLD1 -> pojo.preloadSoundLocation = JsonUtils.readStringList(reader);

                    case GunDisplayTag.CONTROLLABLE_DATA, GunDisplayTag.CONTROLLABLE_DATA_OLD1 -> pojo.controllableData = _ControllableData.fromJson(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, GunDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.SLOT_TEXTURE_LOCATION, this.getSlotTextureLocation());

            JsonUtils.writeResourceLocation(writer, GunDisplayTag.HUD_TEXTURE_LOCATION, this.hudTextureLocation);
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.HUD_EMPTY_TEXTURE_LOCATION, this.hudEmptyTextureLocation);

            JsonUtils.writeToString(writer, GunDisplayTag.GUN_MODEL_TYPE, this.gunModelType);
            JsonUtils.write(writer, GunDisplayTag.LOD_DISPLAY, this.lodDisplay, _LodDisplay::toJson);
            JsonUtils.writeBoolean(writer, GunDisplayTag.ENABLE_TRANSPARENCY, this.enableTransparency);

            JsonUtils.writeFloat(writer, GunDisplayTag.IRON_ZOOM_SCALE, this.ironZoomScale);
            JsonUtils.writeFloat(writer, GunDisplayTag.IRON_VIEW_FOV, this.ironViewFov);
            JsonUtils.writeBoolean(writer, GunDisplayTag.ENABLE_CROSSHAIR, this.enableCrosshair);
            JsonUtils.write(writer, GunDisplayTag.MUZZLE_FLASH_DISPLAY, this.muzzleFlashDisplay, _MuzzleFlashDisplay::toJson);
            JsonUtils.write(writer, GunDisplayTag.LASER_DISPLAY, this.laserDisplay, _LaserDisplay::toJson);
            JsonUtils.writeString2ObjectMap(writer, GunDisplayTag.SURROUND_DISPLAY_BY_HOTBAR, this.surroundDisplayByHotbar, _SurroundDisplay::toJson);
            JsonUtils.write(writer, GunDisplayTag.SURROUND_DISPLAY_BY_OFFHAND, this.surroundDisplayByOffhand, _SurroundDisplay::toJson);
            JsonUtils.writeToString(writer, GunDisplayTag.DAMAGE_DISPLAY_TYPE, this.damageDisplayType);
            JsonUtils.writeToString(writer, GunDisplayTag.AMMO_COUNT_TYPE, this.ammoCountType);
            JsonUtils.write(writer, GunDisplayTag.AMMO_DISPLAY_OVERRIDE, this.ammoDisplayOverride, _AmmoDisplayOverride::toJson);

            JsonUtils.writeResourceLocation(writer, GunDisplayTag.GUN_ANIMATION_LOCATION, this.gunAnimationLocation);
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.SCRIPT_LOCATION, this.scriptLocation);
            JsonUtils.writeString2ObjectMap(writer, GunDisplayTag.SCRIPT_PARAM, this.scriptParam, JsonUtils::writeObject);
            JsonUtils.write(writer, GunDisplayTag.SHELL_EJECTION_PARAM, this.shellEjectionParam, _ShellEjectionParam::toJson);
            JsonUtils.writeToString(writer, GunDisplayTag.THIRD_PERSON_ANIMATION_TYPE, this.thirdPersonAnimationType);
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.PLAYER_ANIMATOR_LOCATION, this.playerAnimatorLocation);
            JsonUtils.writeBoolean(writer, GunDisplayTag.PLAYER_ANIMATOR_FIXED_HAND, this.playerAnimatorFixedHand);
            JsonUtils.writeObject2ObjectMap(writer, GunDisplayTag.GUN_SOUNDS, this.gunSounds, GunSoundType::toString, JsonUtils::writeResourceLocationValue);
            JsonUtils.writeStringList(writer, GunDisplayTag.PRELOAD_SOUND_LOCATION, this.preloadSoundLocation);

            JsonUtils.write(writer, GunDisplayTag.CONTROLLABLE_DATA, this.controllableData, _ControllableData::toJson);
        }
        writer.endObject();
    }

    // --------Getter & Setter--------

    public ResourceLocation getHudTextureLocation() {
        return hudTextureLocation;
    }
    public ResourceLocation getHudEmptyTextureLocation() {
        return hudEmptyTextureLocation;
    }
    public GunModelType getGunModelType() {
        return gunModelType;
    }
    public _LodDisplay getLodDisplay() {
        return lodDisplay;
    }
    public boolean getEnableTransparency() {
        return enableTransparency;
    }
    public float getIronZoomScale() {
        return ironZoomScale;
    }
    public float getIronViewFov() {
        return ironViewFov;
    }
    public boolean getEnableCrosshair() {
        return enableCrosshair;
    }
    public _MuzzleFlashDisplay getMuzzleFlashDisplay() {
        return muzzleFlashDisplay;
    }
    public _LaserDisplay getLaserDisplay() {
        return laserDisplay;
    }
    public Map<String, _SurroundDisplay> getSurroundDisplayByHotbar() {
        return surroundDisplayByHotbar;
    }
    public _SurroundDisplay getSurroundDisplayByOffhand() {
        return surroundDisplayByOffhand;
    }
    public DamageDisplayType getDamageDisplayType() {
        return damageDisplayType;
    }
    public AmmoCountType getAmmoCountType() {
        return ammoCountType;
    }
    public _AmmoDisplayOverride getAmmoDisplayOverride() {
        return ammoDisplayOverride;
    }
    public ResourceLocation getGunAnimationLocation() {
        return gunAnimationLocation;
    }
    public ResourceLocation getScriptLocation() {
        return scriptLocation;
    }
    public Map<String, Object> getScriptParam() {
        return scriptParam;
    }
    public _ShellEjectionParam getShellEjectionParam() {
        return shellEjectionParam;
    }
    public ThirdPersonAnimationType getThirdPersonAnimationType() {
        return thirdPersonAnimationType;
    }
    public ResourceLocation getPlayerAnimatorLocation() {
        return playerAnimatorLocation;
    }
    public boolean getPlayerAnimatorFixedHand() {
        return playerAnimatorFixedHand;
    }
    public Map<GunSoundType, ResourceLocation> getGunSounds() {
        return gunSounds;
    }
    public List<String> getPreloadSoundLocation() {
        return preloadSoundLocation;
    }
    public _ControllableData getControllableData() {
        return controllableData;
    }

    public void setHudTextureLocation(ResourceLocation hudTextureLocation) {
        this.hudTextureLocation = hudTextureLocation;
    }
    public void setHudEmptyTextureLocation(ResourceLocation hudEmptyTextureLocation) {
        this.hudEmptyTextureLocation = hudEmptyTextureLocation;
    }
    public void setGunModelType(GunModelType gunModelType) {
        this.gunModelType = gunModelType;
    }
    public void setLodDisplay(_LodDisplay lodDisplay) {
        this.lodDisplay = lodDisplay;
    }
    public void setEnableTransparency(boolean enableTransparency) {
        this.enableTransparency = enableTransparency;
    }
    public void setIronZoomScale(float ironZoomScale) {
        this.ironZoomScale = ironZoomScale;
    }
    public void setIronViewFov(float ironViewFov) {
        this.ironViewFov = ironViewFov;
    }
    public void setEnableCrosshair(boolean enableCrosshair) {
        this.enableCrosshair = enableCrosshair;
    }
    public void setMuzzleFlashDisplay(_MuzzleFlashDisplay muzzleFlashDisplay) {
        this.muzzleFlashDisplay = muzzleFlashDisplay;
    }
    public void setLaserDisplay(_LaserDisplay laserDisplay) {
        this.laserDisplay = laserDisplay;
    }
    public void setSurroundDisplayByHotbar(Map<String, _SurroundDisplay> surroundDisplayByHotbar) {
        this.surroundDisplayByHotbar = surroundDisplayByHotbar;
    }
    public void setSurroundDisplayByOffhand(_SurroundDisplay surroundDisplayByOffhand) {
        this.surroundDisplayByOffhand = surroundDisplayByOffhand;
    }
    public void setDamageDisplayType(DamageDisplayType damageDisplayType) {
        this.damageDisplayType = damageDisplayType;
    }
    public void setAmmoCountType(AmmoCountType ammoCountType) {
        this.ammoCountType = ammoCountType;
    }
    public void setAmmoDisplayOverride(_AmmoDisplayOverride ammoDisplayOverride) {
        this.ammoDisplayOverride = ammoDisplayOverride;
    }
    public void setGunAnimationLocation(ResourceLocation gunAnimationLocation) {
        this.gunAnimationLocation = gunAnimationLocation;
    }
    public void setScriptLocation(ResourceLocation scriptLocation) {
        this.scriptLocation = scriptLocation;
    }
    public void setScriptParam(Map<String, Object> scriptParam) {
        this.scriptParam = scriptParam;
    }
    public void setShellEjectionParam(_ShellEjectionParam shellEjectionParam) {
        this.shellEjectionParam = shellEjectionParam;
    }
    public void setThirdPersonAnimationType(ThirdPersonAnimationType thirdPersonAnimationType) {
        this.thirdPersonAnimationType = thirdPersonAnimationType;
    }
    public void setPlayerAnimatorLocation(ResourceLocation playerAnimatorLocation) {
        this.playerAnimatorLocation = playerAnimatorLocation;
    }
    public void setPlayerAnimatorFixedHand(boolean playerAnimatorFixedHand) {
        this.playerAnimatorFixedHand = playerAnimatorFixedHand;
    }
    public void setGunSounds(Map<GunSoundType, ResourceLocation> gunSounds) {
        this.gunSounds = gunSounds;
    }
    public void setPreloadSoundLocation(List<String> preloadSoundLocation) {
        this.preloadSoundLocation = preloadSoundLocation;
    }
    public void setControllableData(_ControllableData controllableData) {
        this.controllableData = controllableData;
    }
}