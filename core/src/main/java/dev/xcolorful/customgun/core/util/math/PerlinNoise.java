/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.util.math;

import dev.xcolorful.customgun.core.util.MathUtil;

/**
 * Go to {@link MathUtil.SmoothRandomNoise}
 * @deprecated 这照片是你吗？<br>柏林：这就不是我
 */
@Deprecated(forRemoval = true)
public class PerlinNoise extends MathUtil.SmoothRandomNoise {

    public PerlinNoise(float rangeDown, float rangeUp, long periodMs) {
        super(rangeDown, rangeUp, periodMs);
    }
}
