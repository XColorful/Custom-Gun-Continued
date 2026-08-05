/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.common;

/**
 * 等价于 net.minecraftforge.api.distmarker.Dist
 */
public enum McSide {
    CLIENT,
    DEDICATED_SERVER;

    public boolean isServerSide() {
        return this == DEDICATED_SERVER;
    }

    public boolean isClientSide() {
        return this == CLIENT;
    }
}
