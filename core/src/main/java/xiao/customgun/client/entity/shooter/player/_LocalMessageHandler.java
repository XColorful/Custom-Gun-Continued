/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.entity.shooter.ILocalShooterGetter;
import xiao.customgun.client.api.event.player.SwapItemWithOffHandEvent;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;

public class _LocalMessageHandler implements ICustomEventHandler {
    private static class _LocalMessageHandlerHolder {
        private static final _LocalMessageHandler INSTANCE = new _LocalMessageHandler();
    }
    public static _LocalMessageHandler get() {
        return _LocalMessageHandlerHolder.INSTANCE;
    }
    protected _LocalMessageHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        if (eventType == CustomEventType.SWAP_ITEM_WITH_OFFHAND_EVENT) {
            onSwapItemWithOffhand((SwapItemWithOffHandEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void onSwapItemWithOffhand(SwapItemWithOffHandEvent event) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        ILocalShooterGetter.fromLocalPlayer(localPlayer).cgc$clientDraw(localPlayer.getMainHandItem());
    }
}
