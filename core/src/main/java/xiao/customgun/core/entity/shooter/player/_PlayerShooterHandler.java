/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.event.IPlayerRespawnEvent;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.config.GunConfig;

public class _PlayerShooterHandler implements IEventHandler {
    private static class _PlayerShooterHandlerHoler {
        private static final _PlayerShooterHandler INSTANCE = new _PlayerShooterHandler();
    }
    public static _PlayerShooterHandler get() {
        return _PlayerShooterHandlerHoler.INSTANCE;
    }
    protected _PlayerShooterHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.PLAYER_RESPAWN_EVENT) {
            this.onPlayerRespawn((IPlayerRespawnEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    /**
     * 重生自动换弹
     * TODO 原模组是{@link GunScriptApi}驱动的，逻辑是否对应? 这个功能放在扩展模组比较好
     */
    public void onPlayerRespawn(IPlayerRespawnEvent event) {
        if (event.isEndConquered()
                || !GunConfig.AUTO_RELOAD_WHEN_RESPAWN.get()) return;

        Player player = event.getEntity();
        ItemStack gunItem = player.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(player);
        iLivingShooter.cgc$reload();
    }
}
