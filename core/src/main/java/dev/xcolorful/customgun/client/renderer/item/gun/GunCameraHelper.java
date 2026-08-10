/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item.gun;

import dev.xcolorful.customgun.core.util.MathUtil;

public class GunCameraHelper {
    /**
     * 默认同时只有一个camera，用于平滑 FOV 变化
     */
    public static class State {
        public static final MathUtil.SecondOrderDynamics WORLD_FOV_DYNAMICS = new MathUtil.SecondOrderDynamics(0.5f, 1.2f, 0.5f, 0);
        public static final MathUtil.SecondOrderDynamics ITEM_MODEL_FOV_DYNAMICS = new MathUtil.SecondOrderDynamics(0.5f, 1.2f, 0.5f, 0);
    }

    // TODO
}
