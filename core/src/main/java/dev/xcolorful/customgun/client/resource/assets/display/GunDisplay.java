/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.api.item.gun.DamageDisplayType;
import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import dev.xcolorful.customgun.client.api.item.gun.ShooterAnimationCategory;
import dev.xcolorful.customgun.client.api.model.gun.GunModelType;
import dev.xcolorful.customgun.client.api.model.gun.IGunModelType;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.resource.assets.display.gun.*;
import dev.xcolorful.customgun.core.api.item.gun.AmmoCountType;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.display.GunDisplayTag;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GunDisplay extends _AssetsDisplay<GunDisplay> {

    // 材质
    private @Nullable ResourceLocation hudTextureLocation;
    private @Nullable ResourceLocation hudEmptyTextureLocation;

    // 模型
    private @Nullable IGunModelType gunModelType;
    private @Nullable _LodDisplay lodDisplay;
    private boolean enableTransparency = false;

    // 显示
    private float ironZoomScale = 1.2f;
    private float ironViewFov = 70f;
    private boolean enableCrosshair = false;
    private @Nullable _MuzzleFlashDisplay muzzleFlashDisplay;
    private Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay;
    private @Nullable _LaserDisplay laserDisplay;
    private @Nullable Map<String, _SurroundDisplay> surroundDisplayByHotbar;
    private _SurroundDisplay surroundDisplayByOffhand;
    private DamageDisplayType damageDisplayType;
    private AmmoCountType ammoCountType;
    private @Nullable _AmmoDisplayOverride ammoDisplayOverride;

    // 动画
    private ResourceLocation gunAnimationLocation;
    private @Nullable ResourceLocation scriptLocation;
    private @Nullable Map<String, Object> scriptParam;
    private @Nullable _ShellEjectionParam shellEjectionParam;
    private IShooterAnimationCategory shooterAnimationCategory;
    private @Nullable ResourceLocation playerAnimatorLocation;
    private boolean playerAnimatorFixedHand = false;
    private Map<GunSoundType, ResourceLocation> gunSounds;
    private @Nullable List<ResourceLocation> preloadSoundLocation;

    private @Nullable _ControllableData controllableData;

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
                    case GunDisplayTag.MODEL_TRANSFORM, GunDisplayTag.MODEL_TRANSFORM_OLD1 -> pojo.setModelTransform(JsonUtils.read(reader, _ModelTransform::fromJson));
                    case GunDisplayTag.TEXTURE_LOCATION, GunDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    case GunDisplayTag.SLOT_TEXTURE_LOCATION, GunDisplayTag.SLOT_TEXTURE_LOCATION_OLD1 -> pojo.setSlotTextureLocation(JsonUtils.readResourceLocation(reader));

                    case GunDisplayTag.HUD_TEXTURE_LOCATION, GunDisplayTag.HUD_TEXTURE_LOCATION_OLD1 -> pojo.hudTextureLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.HUD_EMPTY_TEXTURE_LOCATION, GunDisplayTag.HUD_EMPTY_TEXTURE_LOCATION_OLD1 -> pojo.hudEmptyTextureLocation = JsonUtils.readResourceLocation(reader);

                    case GunDisplayTag.GUN_MODEL_TYPE, GunDisplayTag.GUN_MODEL_TYPE_OLD1 -> pojo.gunModelType = JsonUtils.readFromString(reader, GunModelType::fromString);
                    case GunDisplayTag.LOD_DISPLAY, GunDisplayTag.LOD_DISPLAY_OLD1 -> pojo.lodDisplay = JsonUtils.read(reader, _LodDisplay::fromJson);
                    case GunDisplayTag.ENABLE_TRANSPARENCY -> pojo.enableTransparency = JsonUtils.readBoolean(reader);

                    case GunDisplayTag.IRON_ZOOM_SCALE, GunDisplayTag.IRON_ZOOM_SCALE_OLD1 -> pojo.ironZoomScale = JsonUtils.readFloat(reader);
                    case GunDisplayTag.IRON_VIEW_FOV, GunDisplayTag.IRON_VIEW_FOV_OLD1 -> pojo.ironViewFov = JsonUtils.readFloat(reader);
                    case GunDisplayTag.ENABLE_CROSSHAIR, GunDisplayTag.ENABLE_CROSSHAIR_OLD1 -> pojo.enableCrosshair = JsonUtils.readBoolean(reader);
                    case GunDisplayTag.MUZZLE_FLASH_DISPLAY, GunDisplayTag.MUZZLE_FLASH_DISPLAY_OLD1 -> pojo.muzzleFlashDisplay = JsonUtils.read(reader, _MuzzleFlashDisplay::fromJson);
                    case GunDisplayTag.MODEL_NODE_TEXT_DISPLAY, GunDisplayTag.MODEL_NODE_TEXT_DISPLAY_OLD1 -> pojo.modelNodeTextDisplay = JsonUtils.readString2ObjectMap(reader, _ModelNodeTextDisplay::fromJson);
                    case GunDisplayTag.LASER_DISPLAY, GunDisplayTag.LASER_DISPLAY_OLD1 -> pojo.laserDisplay = JsonUtils.read(reader, _LaserDisplay::fromJson);
                    case GunDisplayTag.SURROUND_DISPLAY_BY_HOTBAR, GunDisplayTag.SURROUND_DISPLAY_BY_HOTBAR_OLD1 -> pojo.surroundDisplayByHotbar = JsonUtils.readString2ObjectMap(reader, _SurroundDisplay::fromJson);
                    case GunDisplayTag.SURROUND_DISPLAY_BY_OFFHAND, GunDisplayTag.SURROUND_DISPLAY_BY_OFFHAND_OLD1 -> pojo.surroundDisplayByOffhand = JsonUtils.read(reader, _SurroundDisplay::fromJson);
                    case GunDisplayTag.DAMAGE_DISPLAY_TYPE, GunDisplayTag.DAMAGE_DISPLAY_TYPE_OLD1 -> pojo.damageDisplayType = JsonUtils.readFromString(reader, DamageDisplayType::fromString);
                    case GunDisplayTag.AMMO_COUNT_TYPE, GunDisplayTag.AMMO_COUNT_TYPE_OLD1 -> pojo.ammoCountType = JsonUtils.readFromString(reader, AmmoCountType::fromString);
                    case GunDisplayTag.AMMO_DISPLAY_OVERRIDE, GunDisplayTag.AMMO_DISPLAY_OVERRIDE_OLD1 -> pojo.ammoDisplayOverride = JsonUtils.read(reader, _AmmoDisplayOverride::fromJson);

                    case GunDisplayTag.GUN_ANIMATION_LOCATION, GunDisplayTag.GUN_ANIMATION_LOCATION_OLD1 -> pojo.gunAnimationLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.SCRIPT_LOCATION, GunDisplayTag.SCRIPT_LOCATION_OLD1 -> pojo.scriptLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.SCRIPT_PARAM, GunDisplayTag.SCRIPT_PARAM_OLD1 -> pojo.scriptParam = JsonUtils.readString2ObjectMap(reader, JsonUtils::readObject);
                    case GunDisplayTag.SHELL_EJECTION_PARAM, GunDisplayTag.SHELL_EJECTION_PARAM_OLD1 -> pojo.shellEjectionParam = JsonUtils.read(reader, _ShellEjectionParam::fromJson);
                    case GunDisplayTag.SHOOTER_ANIMATION_CATEGORY, GunDisplayTag.SHOOTER_ANIMATION_CATEGORY_OLD1 -> pojo.shooterAnimationCategory = JsonUtils.readFromString(reader, ShooterAnimationCategory::fromString);
                    case GunDisplayTag.PLAYER_ANIMATOR_LOCATION, GunDisplayTag.PLAYER_ANIMATOR_LOCATION_OLD1 -> pojo.playerAnimatorLocation = JsonUtils.readResourceLocation(reader);
                    case GunDisplayTag.PLAYER_ANIMATOR_FIXED_HAND, GunDisplayTag.PLAYER_ANIMATOR_FIXED_HAND_OLD1 -> pojo.playerAnimatorFixedHand = JsonUtils.readBoolean(reader);
                    case GunDisplayTag.GUN_SOUNDS, GunDisplayTag.GUN_SOUNDS_OLD1 -> pojo.gunSounds = JsonUtils.readObject2ObjectMap(reader, GunSoundType::fromString, JsonUtils::readResourceLocation);
                    case GunDisplayTag.PRELOAD_SOUND_LOCATION, GunDisplayTag.PRELOAD_SOUND_LOCATION_OLD1 -> pojo.preloadSoundLocation = JsonUtils.readList(reader, JsonUtils::readResourceLocation);

                    case GunDisplayTag.CONTROLLABLE_DATA, GunDisplayTag.CONTROLLABLE_DATA_OLD1 -> pojo.controllableData = JsonUtils.read(reader, _ControllableData::fromJson);
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
            JsonUtils.write(writer, GunDisplayTag.MODEL_TRANSFORM, this.getModelTransform(), _ModelTransform::toJson);
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
            JsonUtils.writeString2ObjectMap(writer, GunDisplayTag.MODEL_NODE_TEXT_DISPLAY, this.modelNodeTextDisplay, _ModelNodeTextDisplay::toJson);
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
            JsonUtils.writeToString(writer, GunDisplayTag.SHOOTER_ANIMATION_CATEGORY, this.shooterAnimationCategory);
            JsonUtils.writeResourceLocation(writer, GunDisplayTag.PLAYER_ANIMATOR_LOCATION, this.playerAnimatorLocation);
            JsonUtils.writeBoolean(writer, GunDisplayTag.PLAYER_ANIMATOR_FIXED_HAND, this.playerAnimatorFixedHand);
            JsonUtils.writeObject2ObjectMap(writer, GunDisplayTag.GUN_SOUNDS, this.gunSounds, GunSoundType::toString, JsonUtils::writeResourceLocationValue);
            JsonUtils.writeList(writer, GunDisplayTag.PRELOAD_SOUND_LOCATION, this.preloadSoundLocation, JsonUtils::writeResourceLocationValue);

            JsonUtils.write(writer, GunDisplayTag.CONTROLLABLE_DATA, this.controllableData, _ControllableData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        super.validatePojo();
        if (!this.isValid()) return;

        boolean n1 = (this.getModelTransform() == null | this.getSlotTextureLocation() == null | this.modelNodeTextDisplay == null | this.surroundDisplayByOffhand == null | this.gunSounds == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        this.getModelTransform().validate();
        if (this.lodDisplay != null) this.lodDisplay.validate();
        if (this.muzzleFlashDisplay != null) this.muzzleFlashDisplay.validate();
        if (this.laserDisplay != null) this.laserDisplay.validate();
        this.surroundDisplayByOffhand.validate();
        if (this.ammoDisplayOverride != null) this.ammoDisplayOverride.validate();
        if (this.shellEjectionParam != null) this.shellEjectionParam.validate();
        if (this.controllableData != null) this.controllableData.validate();
        boolean v1 = (this.getModelTransform().isValid() & (this.lodDisplay == null || this.lodDisplay.isValid()) & (this.muzzleFlashDisplay == null || this.muzzleFlashDisplay.isValid()));
        boolean v2 = ((this.laserDisplay == null || this.laserDisplay.isValid()) & this.surroundDisplayByOffhand.isValid() & (this.ammoDisplayOverride == null || this.ammoDisplayOverride.isValid()));
        boolean v3 = ((this.shellEjectionParam == null || this.shellEjectionParam.isValid()) & (this.controllableData == null || this.controllableData.isValid()));
        if (!(v1 & v2 & v3)) {
            this.setValid(false);
            return;
        }

        if (this.surroundDisplayByHotbar != null) for (_SurroundDisplay data : this.surroundDisplayByHotbar.values()) {
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }
        for (_ModelNodeTextDisplay data : this.modelNodeTextDisplay.values()) {
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public @Nullable ResourceLocation getHudTextureLocation() {
        return hudTextureLocation;
    }
    public @Nullable ResourceLocation getHudEmptyTextureLocation() {
        return hudEmptyTextureLocation;
    }
    public @Nullable IGunModelType getGunModelType() {
        return gunModelType;
    }
    public @Nullable _LodDisplay getLodDisplay() {
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
    public @Nullable _MuzzleFlashDisplay getMuzzleFlashDisplay() {
        return muzzleFlashDisplay;
    }
    public Map<String, _ModelNodeTextDisplay> getModelNodeTextDisplay() {
        return modelNodeTextDisplay;
    }
    public @Nullable _LaserDisplay getLaserDisplay() {
        return laserDisplay;
    }
    public @Nullable Map<String, _SurroundDisplay> getSurroundDisplayByHotbar() {
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
    public @Nullable _AmmoDisplayOverride getAmmoDisplayOverride() {
        return ammoDisplayOverride;
    }
    public ResourceLocation getGunAnimationLocation() {
        return gunAnimationLocation;
    }
    public @Nullable ResourceLocation getScriptLocation() {
        return scriptLocation;
    }
    public @Nullable Map<String, Object> getScriptParam() {
        return scriptParam;
    }
    public @Nullable _ShellEjectionParam getShellEjectionParam() {
        return shellEjectionParam;
    }
    public IShooterAnimationCategory getShooterAnimationCategory() {
        return shooterAnimationCategory;
    }
    public @Nullable ResourceLocation getPlayerAnimatorLocation() {
        return playerAnimatorLocation;
    }
    public boolean getPlayerAnimatorFixedHand() {
        return playerAnimatorFixedHand;
    }
    public Map<GunSoundType, ResourceLocation> getGunSounds() {
        return gunSounds;
    }
    public @Nullable List<ResourceLocation> getPreloadSoundLocation() {
        return preloadSoundLocation;
    }
    public @Nullable _ControllableData getControllableData() {
        return controllableData;
    }

    public void setHudTextureLocation(ResourceLocation hudTextureLocation) {
        this.hudTextureLocation = hudTextureLocation;
    }
    public void setHudEmptyTextureLocation(ResourceLocation hudEmptyTextureLocation) {
        this.hudEmptyTextureLocation = hudEmptyTextureLocation;
    }
    public void setGunModelType(IGunModelType gunModelType) {
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
    public void setModelNodeTextDisplay(Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay) {
        this.modelNodeTextDisplay = modelNodeTextDisplay;
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
    public void setShooterAnimationCategory(IShooterAnimationCategory shooterAnimationCategory) {
        this.shooterAnimationCategory = shooterAnimationCategory;
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
    public void setPreloadSoundLocation(List<ResourceLocation> preloadSoundLocation) {
        this.preloadSoundLocation = preloadSoundLocation;
    }
    public void setControllableData(_ControllableData controllableData) {
        this.controllableData = controllableData;
    }

    // --------Back compatibility--------

    @Override
    public GunDisplay applyBackCompatibility() {
        super.applyBackCompatibility();
        this.setModelTransform(this.getModelTransform() == null ? new _ModelTransform().applyBackCompatibility() : this.getModelTransform().applyBackCompatibility());
        this.setSlotTextureLocation(this.getSlotTextureLocation() == null ? ResourceTag.NULL_LOCATION : this.getSlotTextureLocation());

        if (this.lodDisplay != null) this.lodDisplay.applyBackCompatibility();
        if (this.muzzleFlashDisplay != null) this.muzzleFlashDisplay.applyBackCompatibility();
        if (this.laserDisplay != null) this.laserDisplay.applyBackCompatibility();
        if (this.ammoDisplayOverride != null) this.ammoDisplayOverride.applyBackCompatibility();
        if (this.shellEjectionParam != null) this.shellEjectionParam.applyBackCompatibility();
        if (this.controllableData != null) this.controllableData.applyBackCompatibility();

        this.surroundDisplayByOffhand = this.surroundDisplayByOffhand == null ? new _SurroundDisplay().applyBackCompatibility() : this.surroundDisplayByOffhand.applyBackCompatibility();

        if (this.surroundDisplayByHotbar != null) this.surroundDisplayByHotbar.values().forEach(_SurroundDisplay::applyBackCompatibility);
        if (this.modelNodeTextDisplay == null) this.modelNodeTextDisplay = new HashMap<>();
        else this.modelNodeTextDisplay.values().forEach(_ModelNodeTextDisplay::applyBackCompatibility);

        this.gunSounds = this.gunSounds == null ? new HashMap<>() : this.gunSounds;
        return this;
    }
}