/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.common;

import xiao.customgun.CustomGun;

public interface ISideOnly {

    default boolean clientSideOnly() {
        return false;
    }
    default boolean serverSideOnly() {
        return false;
    }
    default boolean inProperSide() {
        return inProperSide(CustomGun.getMcSide());
    }
    default boolean inProperSide(McSide mcSide) {
        if (clientSideOnly() && mcSide == McSide.DEDICATED_SERVER) {
            return false;
        } else return !serverSideOnly() || mcSide != McSide.CLIENT;
    }
}
