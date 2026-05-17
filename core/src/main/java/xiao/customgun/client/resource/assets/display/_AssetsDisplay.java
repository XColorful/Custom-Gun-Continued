/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private Identifier modelLocation;
    private _TransformScale transformScale;
    private ResourceLocation textureLocation;
    private ResourceLocation slotTextureLocation;

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public final ResourceLocation getModelLocation() {
        return modelLocation;
    }
    public final _TransformScale getTransformScale() {
        return transformScale;
    }
    public final ResourceLocation getTextureLocation() {
        return textureLocation;
    }
    public final ResourceLocation getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public final void setModelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setTransformScale(_TransformScale transformScale) {
        this.transformScale = transformScale;
    }
    public final void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }
    public final void setSlotTextureLocation(ResourceLocation slotTextureLocation) {
        this.slotTextureLocation = slotTextureLocation;
    }
}