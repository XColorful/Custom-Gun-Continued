/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.network.message;

import dev.xcolorful.customgun.client.gui.GunRefitScreen;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _ServerMessageRefreshRefitScreen {

    public static void updateScreen() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && ClientGuiUtils.getCurrentScreen(mc) instanceof GunRefitScreen screen) {
            // 刷新Screen
            screen.init();

            // 刷新客户端配件数据
            ShooterGunModifierManager.postChangeEvent(player);
        }
    }
}
