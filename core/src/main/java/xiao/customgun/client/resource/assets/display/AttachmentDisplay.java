/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.sound.attachment.AttachmentSoundType;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.display.AttachmentDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class AttachmentDisplay extends _AssetsDisplay<AttachmentDisplay> {

    // 模型
    private @Nullable _LodDisplay lodDisplay;
    private String adapterNodeName;

    // 显示
    private boolean enableSight = false;
    private boolean enableScope = false;
    private float[] scopeZoomScale;
    private int[] scopeViewIndex;
    private float[] scopeViewFov;
    private boolean showMuzzle = false;
    private boolean showMount = true;
    private Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay;
    private _LaserDisplay laserDisplay;
    private Map<AttachmentSoundType, Identifier> attachmentSounds;

    private static final AttachmentDisplay PARSER = new AttachmentDisplay();
    public static AttachmentDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AttachmentDisplay fromJsonReader(JsonReader reader) throws IOException {
        AttachmentDisplay pojo = new AttachmentDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AttachmentDisplayTag.MODEL_LOCATION, AttachmentDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case AttachmentDisplayTag.TEXTURE_LOCATION, AttachmentDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));
                    case AttachmentDisplayTag.SLOT_TEXTURE_LOCATION, AttachmentDisplayTag.SLOT_TEXTURE_LOCATION_OLD1 -> pojo.setSlotTextureLocation(JsonUtils.readResourceLocation(reader));

                    case AttachmentDisplayTag.LOD_DISPLAY, AttachmentDisplayTag.LOD_DISPLAY_OLD1 -> pojo.lodDisplay = _LodDisplay.fromJson(reader);
                    case AttachmentDisplayTag.ADAPTER_NODE_NAME, AttachmentDisplayTag.ADAPTER_NODE_NAME_OLD1 -> pojo.adapterNodeName = JsonUtils.readString(reader);

                    case AttachmentDisplayTag.ENABLE_SIGHT -> pojo.enableSight = JsonUtils.readBoolean(reader);
                    case AttachmentDisplayTag.ENABLE_SCOPE -> pojo.enableScope = JsonUtils.readBoolean(reader);
                    case AttachmentDisplayTag.SCOPE_ZOOM_SCALE, AttachmentDisplayTag.SCOPE_ZOOM_SCALE_OLD1 -> pojo.scopeZoomScale = JsonUtils.readFloatArray(reader);
                    case AttachmentDisplayTag.SCOPE_VIEW_INDEX, AttachmentDisplayTag.SCOPE_VIEW_INDEX_OLD1 -> pojo.scopeViewIndex = JsonUtils.readIntArray(reader);
                    case AttachmentDisplayTag.SCOPE_VIEW_FOV, AttachmentDisplayTag.SCOPE_VIEW_FOV_OLD2 -> pojo.scopeViewFov = JsonUtils.readFloatArray(reader); case AttachmentDisplayTag.SCOPE_VIEW_FOV_OLD1 -> pojo.scopeViewFov = new float[]{JsonUtils.readFloat(reader)};
                    case AttachmentDisplayTag.SHOW_MUZZLE -> pojo.showMuzzle = JsonUtils.readBoolean(reader);
                    case AttachmentDisplayTag.SHOW_MOUNT -> pojo.showMount = JsonUtils.readBoolean(reader);
                    case AttachmentDisplayTag.MODEL_NODE_TEXT_DISPLAY, AttachmentDisplayTag.MODEL_NODE_TEXT_DISPLAY_OLD1 -> pojo.modelNodeTextDisplay = JsonUtils.readString2ObjectMap(reader, _ModelNodeTextDisplay::fromJson);
                    case AttachmentDisplayTag.LASER_DISPLAY, AttachmentDisplayTag.LASER_DISPLAY_OLD1 -> pojo.laserDisplay = _LaserDisplay.fromJson(reader);
                    case AttachmentDisplayTag.ATTACHMENT_SOUNDS, AttachmentDisplayTag.ATTACHMENT_SOUNDS_OLD1 -> pojo.attachmentSounds = JsonUtils.readObject2ObjectMap(reader, AttachmentSoundType::fromString, JsonUtils::readResourceLocation);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AttachmentDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, AttachmentDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, AttachmentDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());
            JsonUtils.writeResourceLocation(writer, AttachmentDisplayTag.SLOT_TEXTURE_LOCATION, this.getSlotTextureLocation());

            JsonUtils.write(writer, AttachmentDisplayTag.LOD_DISPLAY, this.lodDisplay, _LodDisplay::toJson);
            JsonUtils.writeString(writer, AttachmentDisplayTag.ADAPTER_NODE_NAME, this.adapterNodeName);

            JsonUtils.writeBoolean(writer, AttachmentDisplayTag.ENABLE_SIGHT, this.enableSight);
            JsonUtils.writeBoolean(writer, AttachmentDisplayTag.ENABLE_SCOPE, this.enableScope);
            JsonUtils.writeFloatArray(writer, AttachmentDisplayTag.SCOPE_ZOOM_SCALE, this.scopeZoomScale);
            JsonUtils.writeIntArray(writer, AttachmentDisplayTag.SCOPE_VIEW_INDEX, this.scopeViewIndex);
            JsonUtils.writeFloatArray(writer, AttachmentDisplayTag.SCOPE_VIEW_FOV, this.scopeViewFov);
            JsonUtils.writeBoolean(writer, AttachmentDisplayTag.SHOW_MUZZLE, this.showMuzzle);
            JsonUtils.writeBoolean(writer, AttachmentDisplayTag.SHOW_MOUNT, this.showMount);
            JsonUtils.writeString2ObjectMap(writer, AttachmentDisplayTag.MODEL_NODE_TEXT_DISPLAY, this.modelNodeTextDisplay, _ModelNodeTextDisplay::toJson);
            JsonUtils.write(writer, AttachmentDisplayTag.LASER_DISPLAY, this.laserDisplay, _LaserDisplay::toJson);
            JsonUtils.writeObject2ObjectMap(writer, AttachmentDisplayTag.ATTACHMENT_SOUNDS, this.attachmentSounds, AttachmentSoundType::toString, JsonUtils::writeResourceLocationValue);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        super.validatePojo();
        if (!this.isValid()) return;

        boolean n1 = (this.getSlotTextureLocation() == null | this.adapterNodeName == null | this.scopeZoomScale == null);
        boolean n2 = (this.scopeViewFov == null | this.modelNodeTextDisplay == null | this.laserDisplay == null | this.attachmentSounds == null | this.scopeViewIndex == null);
        if (n1 | n2) {
            this.setValid(false);
            return;
        }
        if (this.lodDisplay != null) this.lodDisplay.validate();
        this.laserDisplay.validate();
        boolean v1 = ((this.lodDisplay == null || this.lodDisplay.isValid()) & this.laserDisplay.isValid());
        if (!v1) {
            this.setValid(false);
            return;
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

    public boolean getEnableSight() {
        return enableSight;
    }
    public boolean getEnableScope() {
        return enableScope;
    }
    public @Nullable _LodDisplay getLodDisplay() {
        return lodDisplay;
    }
    public String getAdapterNodeName() {
        return adapterNodeName;
    }
    public float[] getScopeZoomScale() {
        return scopeZoomScale;
    }
    public int[] getScopeViewIndex() {
        return scopeViewIndex;
    }
    public float[] getScopeViewFov() {
        return scopeViewFov;
    }
    public boolean getShowMuzzle() {
        return showMuzzle;
    }
    public boolean getShowMount() {
        return showMount;
    }
    public Map<String, _ModelNodeTextDisplay> getModelNodeTextDisplay() {
        return modelNodeTextDisplay;
    }
    public _LaserDisplay getLaserDisplay() {
        return laserDisplay;
    }
    public Map<AttachmentSoundType, Identifier> getAttachmentSounds() {
        return attachmentSounds;
    }

    public void setEnableSight(boolean enableSight) {
        this.enableSight = enableSight;
    }
    public void setEnableScope(boolean enableScope) {
        this.enableScope = enableScope;
    }
    public void setLodDisplay(_LodDisplay lodDisplay) {
        this.lodDisplay = lodDisplay;
    }
    public void setAdapterNodeName(String adapterNodeName) {
        this.adapterNodeName = adapterNodeName;
    }
    public void setScopeZoomScale(float[] scopeZoomScale) {
        this.scopeZoomScale = scopeZoomScale;
    }
    public void setScopeViewIndex(int[] scopeViewIndex) {
        this.scopeViewIndex = scopeViewIndex;
    }
    public void setScopeViewFov(float[] scopeViewFov) {
        this.scopeViewFov = scopeViewFov;
    }
    public void setShowMuzzle(boolean showMuzzle) {
        this.showMuzzle = showMuzzle;
    }
    public void setShowMount(boolean showMount) {
        this.showMount = showMount;
    }
    public void setModelNodeTextDisplay(Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay) {
        this.modelNodeTextDisplay = modelNodeTextDisplay;
    }
    public void setLaserDisplay(_LaserDisplay laserDisplay) {
        this.laserDisplay = laserDisplay;
    }
    public void setAttachmentSounds(Map<AttachmentSoundType, Identifier> attachmentSounds) {
        this.attachmentSounds = attachmentSounds;
    }

    // --------Back compatibility--------

    @Override
    public AttachmentDisplay applyBackCompatibility() {
        super.applyBackCompatibility();
        this.setSlotTextureLocation(this.getSlotTextureLocation() == null ? ResourceTag.NULL_LOCATION : this.getSlotTextureLocation());

        if (this.lodDisplay != null) this.lodDisplay.applyBackCompatibility();
        this.adapterNodeName = this.adapterNodeName == null ? "" : this.adapterNodeName;

        this.scopeZoomScale = this.scopeZoomScale == null ? new float[]{1f} : this.scopeZoomScale;
        this.scopeViewIndex = this.scopeViewIndex == null ? new int[]{0} : this.scopeViewIndex;
        this.scopeViewFov = this.scopeViewFov == null ? new float[]{70f} : this.scopeViewFov;

        if (this.modelNodeTextDisplay == null) this.modelNodeTextDisplay = new HashMap<>();
        else this.modelNodeTextDisplay.values().forEach(_ModelNodeTextDisplay::applyBackCompatibility);

        this.laserDisplay = this.laserDisplay == null ? new _LaserDisplay().applyBackCompatibility() : this.laserDisplay.applyBackCompatibility();
        this.attachmentSounds = this.attachmentSounds == null ? new HashMap<>() : this.attachmentSounds;
        return this;
    }
}