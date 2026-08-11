/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.interpolator;

import dev.xcolorful.customgun.client.animation.channel.AnimChannelContent;

public interface IInterpolator<T extends IInterpolator<T>> {

    InterpolatorType getType();

    void compile(AnimChannelContent content);

    float[] interpolate(int indexFrom, int indexTo, float alpha);

    T clone();
}
