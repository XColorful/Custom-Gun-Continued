/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.interpolator;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.animation.channel.AnimChannelContent;
import dev.xcolorful.customgun.client.api.animation.interpolator.IInterpolator;
import dev.xcolorful.customgun.client.api.animation.interpolator.InterpolatorType;

public class Linear implements IInterpolator<Linear> {

    private AnimChannelContent content;

    public Linear() {
    }
    @Override public InterpolatorType getType() {
        return InterpolatorType.COMPOSITE;
    }

    @Override
    public void compile(AnimChannelContent content) {
        this.content = content;
    }

    @Override
    public float[] interpolate(int indexFrom, int indexTo, float alpha) {
        // 如果动画值有 6 个，后三个为 Post 数值，用于插值起点
        int offset = content.values[indexFrom].length == 6 ? 3 : 0;
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            if (indexFrom == indexTo) {
                result[i] = content.values[indexFrom][i + offset];
            } else {
                result[i] = content.values[indexFrom][i + offset] * (1 - alpha) + content.values[indexTo][i] * alpha;
            }
        }
        return result;
    }

    @Override
    public Linear clone() {
        try {
            Linear linear = (Linear) super.clone();
            linear.content = this.content;
            return linear;
        } catch (CloneNotSupportedException e) {
            CustomGun.LOGGER.error("Clone not supported.", e);
            throw new RuntimeException(e);
        }
    }
}
