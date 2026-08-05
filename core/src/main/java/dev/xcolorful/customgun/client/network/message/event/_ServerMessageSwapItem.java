/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.network.message.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.player.SwapItemWithOffHandEvent;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageSwapItem;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _ServerMessageSwapItem {

    public static void doClientEvent(ServerMessageSwapItem message) {
        CustomGun.getEventPoster().postCustomEvent(new SwapItemWithOffHandEvent());
    }
}
