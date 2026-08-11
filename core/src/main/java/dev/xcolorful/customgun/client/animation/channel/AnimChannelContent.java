/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.channel;

import dev.xcolorful.customgun.client.api.animation.interpolator.LerpMode;

import java.util.Arrays;

public class AnimChannelContent {

    public float[] keyframeTimeS;
    /**
     * 动画数值。该数组的第一维每个元素都与上面的 keyframeTime 顺序对应，
     * 第二维可以是位移、旋转或缩放的值，其中如果是四元数，数组长度可以为 8 或 4。(长度为 8 时，前四位存 Pre 数值，后四位存 Post 数值)
     * 如果是三轴数值，数组长度可以为 6 或 3，Pre 和 Post 与上同理。
     */
    public float[][] values;
    /**
     * 对于使用一般插值器的 Channel，这个动画值没有意义。它专门用于 CustomInterpolator
     */
    public LerpMode[] lerpModes;

    public AnimChannelContent() {
    }

    public AnimChannelContent(AnimChannelContent source) {
        if (source.keyframeTimeS != null) {
            this.keyframeTimeS = Arrays.copyOf(source.keyframeTimeS, source.keyframeTimeS.length);
        }
        if (source.values != null) {
            // 深拷贝动画数值
            this.values = Arrays.stream(source.values)
                    .map(values -> Arrays.copyOf(values, values.length))
                    .toArray(float[][]::new);
        }
        if (source.lerpModes != null) {
            this.lerpModes = Arrays.copyOf(source.lerpModes, source.lerpModes.length);
        }
    }
}
