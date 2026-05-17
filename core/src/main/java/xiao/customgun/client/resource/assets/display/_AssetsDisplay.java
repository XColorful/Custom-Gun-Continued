/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private ResourceLocation modelLocation;
    private ResourceLocation textureLocation;

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public final ResourceLocation getModelLocation() {
        return modelLocation;
    }
    public final ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public final void setModelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }
}