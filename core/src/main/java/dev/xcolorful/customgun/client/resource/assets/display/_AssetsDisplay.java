/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.display;

import dev.xcolorful.customgun.core.resource.ResourcePojo;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private @Nullable ResourceLocation modelLocation;
    private @Nullable _ModelTransform modelTransform;
    private @Nullable ResourceLocation textureLocation;
    private @Nullable ResourceLocation slotTextureLocation;

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public final @Nullable ResourceLocation getModelLocation() {
        return modelLocation;
    }
    public final @Nullable _ModelTransform getModelTransform() {
        return modelTransform;
    }
    public final @Nullable ResourceLocation getTextureLocation() {
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
}