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
import xiao.customgun.client.gui.GunRefitScreen;
import xiao.customgun.core.entity.gun.GunPropertyManager;

@ApiStatus.Internal
public class _ServerMessageRefreshRefitScreen {

    public static void updateScreen() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && mc.screen instanceof GunRefitScreen screen) {
            // 刷新Screen
            screen.init();

            // 刷新客户端配件数据
            GunPropertyManager.postChangeEvent(player, player.getMainHandItem());
        }
    }
}
