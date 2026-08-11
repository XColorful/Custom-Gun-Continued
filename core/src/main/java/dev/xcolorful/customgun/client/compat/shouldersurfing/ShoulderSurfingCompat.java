/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.compat.shouldersurfing;

public class ShoulderSurfingCompat {

    /**
     * @return 是否显示越肩视角的准心
     */
    public static boolean showCrosshair() {
        // mixin注入点
        return false;
    }

    /**
     * @return 当前是否是越肩视角
     */
    public static boolean isShoulderSurfing() {
        // mixin注入点
        return false;
    }

    public static float getXRot() {
        // mixin注入点
        return 0;
    }
    public static float getYRot() {
        // mixin注入点
        return 0;
    }

    public static void setXRot(float xRot) {
        // mixin注入点
    }
    public static void setYRot(float yRot) {
        // mixin注入点
    }
}
