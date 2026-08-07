/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.listener;

import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;

public interface IAnimationListener {

    ObjectAnimationChannel.ChannelType getType();

    /**
     * @param values 根据 ChannelType 而变化
     *               <ul>
     *                  <li>TRANSLATION：values 长度为 3，存储 xyz 偏移量</li>
     *                  <li>ROTATION：values 长度为 4 或 3；为 4 时表示四元数</li>
     *                  <li>SCALE：values 长度为 3，存储 xyz 缩放值</li>
     *               </ul>
     * @param blend  进行混合时，动画值应当被累加，而不是被直接覆盖
     */
    void update(float[] values, boolean blend);

    float[] initialValue();

}
