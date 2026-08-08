/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.instance.data;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.attachment.AttachmentSoundType;
import dev.xcolorful.customgun.client.model.AttachmentModelObject;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LaserDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LodDisplay;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ClientAttachmentIndexInstance extends PojoInstance<AttachmentIndex> {

    private @Nullable AttachmentModelObject attachmentModel;
    private @Nullable AttachmentModelObject attachmentModelLod;

    private @Nullable AttachmentData attachmentDataCache;
    private AttachmentDisplay attachmentDisplayCache;

    private ClientAttachmentIndexInstance(@NotNull AttachmentIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientAttachmentIndexInstance fromPojo(AttachmentIndex pojo) {
        if (pojo == null) return null;
        ClientAttachmentIndexInstance instance = new ClientAttachmentIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        this.attachmentDisplayCache = ClientResourceApi.getAttachmentDisplay(this.getPojo().getDisplayIndexLocation());
        if (this.attachmentDisplayCache == null) {
            CustomGun.LOGGER.debug("ClientAttachmentIndexInstance: AttachmentDisplay {} not found", this.getPojo().getDisplayIndexLocation());
            return false;
        } else if (!this.attachmentDisplayCache.isValid()) {
            CustomGun.LOGGER.debug("ClientAttachmentIndexInstance: AttachmentDisplay {} not valid", this.getPojo().getDisplayIndexLocation());
            return false;
        }

        {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(this.attachmentDisplayCache.getModelLocation());
            if (bedrockModel != null) {
                this.attachmentModel = AttachmentModelObject.fromPojo(bedrockModel);
                if (this.attachmentModel == null) CustomGun.LOGGER.debug("ClientAttachmentIndexInstance: Failed to create AttachmentModelObject {}", this.attachmentDisplayCache.getModelLocation());
            } else {
                CustomGun.LOGGER.debug("ClientAttachmentIndexInstance: BedrockModel {} not found", this.attachmentDisplayCache.getModelLocation());
            }
        }
        _LodDisplay lodDisplay = this.attachmentDisplayCache.getLodDisplay();
        if (lodDisplay != null) {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(lodDisplay.getModelLocation());
            if (bedrockModel != null) {
                this.attachmentModelLod = AttachmentModelObject.fromPojo(bedrockModel);
                if (this.attachmentModelLod == null) CustomGun.LOGGER.debug("ClientAttachmentIndexInstance: Failed to create AttachmentModelObject (for lod) {}", lodDisplay.getModelLocation());
            } else {
                CustomGun.LOGGER.debug("ClientAttachmentIndexInstance: BedrockModel (for lod) {} not found", lodDisplay.getModelLocation());
            }
        }

        return true;
    }
    private static final int ERR_SCOPE_VIEW_FOV = 1;
    private static final int ERR_SCOPE_ZOOM_SCALE = 1 << 1;
    private static final int ERR_SCOPE_VIEW_INDEX = 1 << 2;
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        int errorMask = 0;
        // AttachmentDisplay
        errorMask |= checkScopeViewFov(this.attachmentDisplayCache) ? ERR_SCOPE_VIEW_FOV : 0;
        errorMask |= checkScopeZoomScale(this.attachmentDisplayCache) ? ERR_SCOPE_ZOOM_SCALE : 0;
        errorMask |= checkScopeViewIndex(this.attachmentDisplayCache) ? ERR_SCOPE_VIEW_INDEX : 0;
        if (errorMask != 0) {
            this.logAllErrors(errorMask);
            return false;
        }

        return true;
    }
    @Override protected void logAllErrors(int errorMask) {
        StringBuilder sb = new StringBuilder("ClientAttachmentIndexInstance: AttachmentDisplay ").append(this.getPojo().getDataLocation()).append(" is invalid because:");
        if ((errorMask & ERR_SCOPE_VIEW_FOV) != 0) sb.append("\n\t- scopeViewFov <= 0");
        if ((errorMask & ERR_SCOPE_ZOOM_SCALE) != 0) sb.append("\n\t- scopeZoomScale < 1");
        if ((errorMask & ERR_SCOPE_VIEW_INDEX) != 0) sb.append("\n\t- scopeViewIndex < 1");
        CustomGun.LOGGER.debug(sb.toString());
    }
    public static boolean checkScopeViewFov(@NotNull AttachmentDisplay pojo) {
        float[] scopeViewFov = pojo.getScopeViewFov();
        if (scopeViewFov == null || scopeViewFov.length == 0) return false;
        for (int i = 0; i < scopeViewFov.length; i++) {
            if (scopeViewFov[i] <= 0) return true;
        }
        return false;
    }
    public static boolean checkScopeZoomScale(@NotNull AttachmentDisplay pojo) {
        float[] scopeZoomScale = pojo.getScopeZoomScale();
        if (scopeZoomScale == null || scopeZoomScale.length == 0) return false;
        for (int i = 0; i < scopeZoomScale.length; i++) {
            if (scopeZoomScale[i] < 1) return true;
        }
        return false;
    }
    public static boolean checkScopeViewIndex(@NotNull AttachmentDisplay pojo) {
        int[] scopeViewIndex = pojo.getScopeViewIndex();
        if (scopeViewIndex == null || scopeViewIndex.length == 0) return false;
        for (int i = 0; i < scopeViewIndex.length; i++) {
            if (scopeViewIndex[i] < 1) return true;
        }
        return false;
    }

    // --------Getter--------

    public @Nullable AttachmentModelObject getAttachmentModel() {
        return this.attachmentModel;
    }
    public @Nullable AttachmentModelObject getAttachmentModelLod() {
        return this.attachmentModelLod;
    }

    public @Nullable AttachmentData getAttachmentData() {
        if (this.attachmentDisplayCache == null) {
            AttachmentData attachmentData = ResourceApi.getAttachmentData(this.getPojo().getDataLocation());
            if (attachmentData != null && attachmentData.isValid()) this.attachmentDataCache = attachmentData;
        }
        return this.attachmentDataCache;
    }
    public AttachmentDisplay getAttachmentDisplay() {
        return this.attachmentDisplayCache;
    }

    // --------Deprecated--------

    @Deprecated public String getName() {
        return ComponentUtils.toTranslatableKey(this.getPojo().getNameLang());
    }
    @Deprecated public String getTooltipKey() {
        return ComponentUtils.toTranslatableKey(this.getPojo().getTooltipLang());
    }
    @Deprecated public boolean isSight() {
        return this.attachmentDisplayCache.getEnableSight();
    }
    @Deprecated public boolean isScope() {
        return this.attachmentDisplayCache.getEnableScope();
    }
    @Deprecated public String getAdapterNodeName() {
        return this.attachmentDisplayCache.getAdapterNodeName();
    }
    @Deprecated public float[] getViewsFov() {
        return this.attachmentDisplayCache.getScopeViewFov();
    }
    @Deprecated public boolean isShowMuzzle() {
        return this.attachmentDisplayCache.getShowMuzzle();
    }
    @Deprecated public boolean isShowMount() {
        return this.attachmentDisplayCache.getShowMount();
    }
    @Deprecated public @Nullable _LaserDisplay getLaserConfig() {
        return this.attachmentDisplayCache.getLaserDisplay();
    }
    @Deprecated public Map<AttachmentSoundType, ResourceLocation> getSounds() {
        return this.attachmentDisplayCache.getAttachmentSounds();
    }
}
