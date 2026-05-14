/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _ServerMessageCraft {

    public static void updateScreen(int containerId) {
        LocalPlayer player = Minecraft.getInstance().player;
        // TODO GunSmithTableScreen
    }
}