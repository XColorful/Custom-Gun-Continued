/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter.player;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.gun.script.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.config.GunConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
        switch (eventType) {
            case PLAYER_RESPAWN_EVENT -> onPlayerRespawn((IPlayerRespawnEvent) event);
            case LEFT_CLICK_BLOCK_EVENT -> preventShootInteraction((ILeftClickBlockEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    /**
     * 重生自动换弹
     * TODO 原模组是{@link GunScriptApi}驱动的，逻辑是否对应? 这个功能放在扩展模组比较好
     */
    private void onPlayerRespawn(IPlayerRespawnEvent event) {
        if (event.isEndConquered()
                || !GunConfig.AUTO_RELOAD_WHEN_RESPAWN.get()) return;

        this.autoReload(event.getEntity());
    }
    private void autoReload(Player player) {
        ItemStack gunItem = player.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(player);
        iLivingShooter.cgc$reload();
    }

    private void preventShootInteraction(ILeftClickBlockEvent event) {
        // 交互手的物品为枪械 -> 取消交互
        if (IGunGetter.fromItemStack(event.getItemStack()) != null) {
            event.setCanceled(true);
        }
    }
}
