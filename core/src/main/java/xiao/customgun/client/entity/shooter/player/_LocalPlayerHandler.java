/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.event.ClientDelayedEvent;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;

import java.util.function.Consumer;

public class _LocalPlayerHandler {
    private static class _LocalPlayerHandlerHolder {
        private static final _LocalPlayerHandler INSTANCE = new _LocalPlayerHandler();
    }
    public static _LocalPlayerHandler get() {
        return _LocalPlayerHandlerHolder.INSTANCE;
    }
    protected _LocalPlayerHandler() {}

    private long currentTimeMillis = 0;
    public long getCurrentTimeMillis() {
        return this.currentTimeMillis;
    }

    public void onClientPlayerClone(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        final long currentTimeMillis = System.currentTimeMillis();
        this.currentTimeMillis = currentTimeMillis;

        Consumer<LocalPlayer> delayedTask = localPlayer -> {
            LocalPlayer currentPlayer = Minecraft.getInstance().player;
            if (currentPlayer != null && currentPlayer == localPlayer) {
                if (currentTimeMillis == _LocalPlayerHandler.get().getCurrentTimeMillis()) {
                    ILivingShooterGetter.cgc$fromLivingEntity(localPlayer).cgc$initLivingShooter();
                }
            }
        };
        // 事件触发时玩家背包未同步，延迟10tick再刷新缓存
        new ClientDelayedEvent<>(delayedTask, newPlayer, 10, "_LocalPlayerHandler onClientPlayerClone");
    }
}
