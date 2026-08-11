/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.entity.projectile;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IClientGunProjectileTracer {

    // --------Getter & Setter--------

    float cgc$getCameraXRot();
    float cgc$getCameraYRot();
    float @Nullable [] cgc$getFirstPersonRenderOffset();
    int cgc$getTracerColorInt(Entity gunProjectile);
    float cgc$getTracerScaleModifier(Entity gunProjectile);

    void cgc$setCameraXRot(float cameraXRot);
    void cgc$setCameraYRot(float cameraYRot);
    void cgc$setFirstPersonRenderOffset(float[] firstPersonRenderOffset);
    void cgc$setTracerColorInt(Entity gunProjectile, int color);
    void cgc$setTracerScaleModifier(Entity gunProjectile, float modifier);
}
