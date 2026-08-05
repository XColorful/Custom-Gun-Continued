/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.entity.shooter;

import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import net.minecraft.client.player.LocalPlayer;

public interface ILocalShooterGetter {

    /**
     * LocalPlayer 通过 Mixin 的方式实现了 ILocalShooter 接口
     */
    static ILocalShooter fromLocalPlayer(LocalPlayer localPlayer) {
        return (ILocalShooter) localPlayer;
    }
}