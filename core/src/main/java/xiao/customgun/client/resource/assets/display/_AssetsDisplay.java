/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import net.minecraft.resources.Identifier;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private Identifier modelLocation;
    private _TransformScale transformScale;
    private Identifier textureLocation;
    private Identifier slotTextureLocation;

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public final Identifier getModelLocation() {
        return modelLocation;
    }
    public final _TransformScale getTransformScale() {
        return transformScale;
    }
    public final Identifier getTextureLocation() {
        return textureLocation;
    }
    public final Identifier getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public final void setModelLocation(Identifier modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setTransformScale(_TransformScale transformScale) {
        this.transformScale = transformScale;
    }
    public final void setTextureLocation(Identifier textureLocation) {
        this.textureLocation = textureLocation;
    }
    public final void setSlotTextureLocation(Identifier slotTextureLocation) {
        this.slotTextureLocation = slotTextureLocation;
    }
}