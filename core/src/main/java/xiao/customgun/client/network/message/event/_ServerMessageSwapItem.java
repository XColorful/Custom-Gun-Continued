/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message.event;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.player.SwapItemWithOffHandEvent;
import xiao.customgun.core.network.message.event.ServerMessageSwapItem;

@ApiStatus.Internal
public class _ServerMessageSwapItem {

    public static void doClientEvent(ServerMessageSwapItem message) {
        CustomGun.getEventPoster().postCustomEvent(new SwapItemWithOffHandEvent());
    }
}
