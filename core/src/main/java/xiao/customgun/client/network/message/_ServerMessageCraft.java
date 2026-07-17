/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _ServerMessageCraft {

    public static void updateScreen(int containerId) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && player.containerMenu.containerId == containerId) {
            updateScreen(player, mc.screen);
        }
    }

    public static void updateScreen(LocalPlayer localPlayer, Screen screen) {
        // mixin注入点
    }
}