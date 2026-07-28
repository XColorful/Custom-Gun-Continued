/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.minecraft;

import net.minecraftforge.eventbus.api.Event;
import xiao.customgun.core.api.minecraft.TriResult;

public class TriResultHelper {

    public static TriResult convert(Event.Result result) {
        return switch (result) {
            case ALLOW -> TriResult.ALLOW;
            case DENY -> TriResult.DENY;
            default -> TriResult.DEFAULT;
        };
    }
    public static Event.Result convert(TriResult result) {
        return switch (result) {
            case ALLOW -> Event.Result.ALLOW;
            case DENY -> Event.Result.DENY;
            default -> Event.Result.DEFAULT;
        };
    }
}
