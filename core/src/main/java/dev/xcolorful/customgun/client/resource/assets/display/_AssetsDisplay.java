/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.display;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private ResourceLocation modelLocation;
    private @Nullable _ModelTransform modelTransform;
    private ResourceLocation textureLocation;
    private @Nullable ResourceLocation slotTextureLocation;

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.modelLocation == null | this.textureLocation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public final ResourceLocation getModelLocation() {
        return modelLocation;
    }
    public final @Nullable _ModelTransform getModelTransform() {
        return modelTransform;
    }
    public final ResourceLocation getTextureLocation() {
        return textureLocation;
    }
    public final @Nullable ResourceLocation getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public final void setModelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setModelTransform(_ModelTransform modelTransform) {
        this.modelTransform = modelTransform;
    }
    public final void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }
    public final void setSlotTextureLocation(ResourceLocation slotTextureLocation) {
        this.slotTextureLocation = slotTextureLocation;
    }

    // --------Back compatibility--------

    @SuppressWarnings("unchecked")
    @Override
    public T applyBackCompatibility() {
        this.modelLocation = this.modelLocation == null ? ResourceTag.NULL_LOCATION : this.modelLocation;
        this.textureLocation = this.textureLocation == null ? ResourceTag.NULL_LOCATION : this.textureLocation;
        return (T) this;
    }
}