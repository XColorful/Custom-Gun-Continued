/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.compat.optifine;

public class OptifineCompat {

    /**
     * @return 是否接管了操作
     */
    public static boolean onEnableItemEntityStencilTest() {
        // mixin注入点
        return false;
    }
}
