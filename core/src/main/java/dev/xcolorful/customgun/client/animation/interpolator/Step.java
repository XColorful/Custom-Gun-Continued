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

import java.util.Arrays;

public class Step implements IInterpolator<Step> {

    private AnimChannelContent content;

    public Step() {
    }
    @Override public InterpolatorType getType() {
        return InterpolatorType.STEP;
    }

    @Override
    public void compile(AnimChannelContent content) {
        this.content = content;
    }

    @Override
    public float[] interpolate(int indexFrom, int indexTo, float alpha) {
        float[] result;
        int offset = switch (content.values[indexFrom].length) {
            case 8 -> 4;
            case 6 -> 3;
            default -> 0;
        };
        if (alpha < 1 || indexFrom == indexTo) {
            result = Arrays.copyOfRange(content.values[indexFrom], offset, content.values[indexFrom].length);
        } else {
            int length = content.values[indexTo].length;
            length = switch (length) {
                case 8 -> 4;
                case 6 -> 3;
                default -> length;
            };
            result = Arrays.copyOfRange(content.values[indexTo], 0, length);
        }
        return result;
    }

    @Override
    public Step clone() {
        try {
            Step step = (Step) super.clone();
            step.content = this.content;
            return step;
        } catch (CloneNotSupportedException e) {
            CustomGun.LOGGER.error("Clone not supported.", e);
            throw new RuntimeException(e);
        }
    }
}
