/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.interpolator;

@Deprecated(forRemoval=true)
public class InterpolatorUtil {

    public static IInterpolator<?> fromInterpolation(InterpolatorType interpolation) {
        return interpolation.create();
    }
}
