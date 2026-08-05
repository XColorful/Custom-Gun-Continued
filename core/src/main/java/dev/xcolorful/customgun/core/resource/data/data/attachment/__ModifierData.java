/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.attachment;

import dev.xcolorful.customgun.core.resource.ResourcePojo;
import org.jetbrains.annotations.Nullable;

public abstract class __ModifierData<T extends __ModifierData<T>> extends ResourcePojo<T> {

    private float sharedBaseAdd = 0;
    private float sharedPercentAdd = 0;
    private float uniqueMultiplier = 1;
    private @Nullable String scriptFunction;

    // --------Getter & Setter--------

    public final float getSharedBaseAdd() {
        return sharedBaseAdd;
    }
    public final  float getSharedPercentAdd() {
        return sharedPercentAdd;
    }
    public final float getUniqueMultiplier() {
        return uniqueMultiplier;
    }
    public final @Nullable String getScriptFunction() {
        return scriptFunction;
    }

    public final void setSharedBaseAdd(float sharedBaseAdd) {
        this.sharedBaseAdd = sharedBaseAdd;
    }
    public final void setSharedPercentAdd(float sharedPercentAdd) {
        this.sharedPercentAdd = sharedPercentAdd;
    }
    public final void setUniqueMultiplier(float uniqueMultiplier) {
        this.uniqueMultiplier = uniqueMultiplier;
    }
    public final void setScriptFunction(String scriptFunction) {
        this.scriptFunction = scriptFunction;
    }
}