/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private Identifier modelLocation;
    private @Nullable _ModelTransform modelTransform;
    private Identifier textureLocation;
    private @Nullable Identifier slotTextureLocation;

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

    public final Identifier getModelLocation() {
        return modelLocation;
    }
    public final @Nullable _ModelTransform getModelTransform() {
        return modelTransform;
    }
    public final Identifier getTextureLocation() {
        return textureLocation;
    }
    public final @Nullable Identifier getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public final void setModelLocation(Identifier modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setModelTransform(_ModelTransform modelTransform) {
        this.modelTransform = modelTransform;
    }
    public final void setTextureLocation(Identifier textureLocation) {
        this.textureLocation = textureLocation;
    }
    public final void setSlotTextureLocation(Identifier slotTextureLocation) {
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