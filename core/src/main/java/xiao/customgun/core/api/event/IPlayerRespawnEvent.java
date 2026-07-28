/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.event;

import net.minecraft.world.entity.player.Player;

public interface IPlayerRespawnEvent extends IEvent {

    Player getEntity();

    boolean isEndConquered();
}
