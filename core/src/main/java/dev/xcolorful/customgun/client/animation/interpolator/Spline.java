/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.interpolator;

import dev.xcolorful.customgun.client.animation.channel.AnimChannelContent;
import dev.xcolorful.customgun.client.api.animation.interpolator.IInterpolator;
import dev.xcolorful.customgun.client.api.animation.interpolator.InterpolatorType;

// TODO
public class Spline implements IInterpolator<Spline> {

    public Spline() {
    }
    @Override public InterpolatorType getType() {
        return InterpolatorType.SPLINE;
    }

    @Override
    public void compile(AnimChannelContent content) {
    }

    @Override
    public float[] interpolate(int indexFrom, int indexTo, float alpha) {
        return new float[]{0, 0, 0, 1};
    }

    @Override
    public Spline clone() {
        return null;
    }
}
