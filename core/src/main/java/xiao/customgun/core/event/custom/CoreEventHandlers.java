/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.event.custom;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.*;
import xiao.customgun.core.entity.LivingShooterSyncHandler;
import xiao.customgun.core.entity.victim.BulletVictimKnockback;
import xiao.customgun.core.projectile.impact._WorldImpactHandler;
import xiao.customgun.core.resource._AllDataManager;

public class CoreEventHandlers {

    public static void registerAll(ICustomEventRegister customEventRegister) {
        register(customEventRegister, _AllDataManager._getInternal(), EventType.ADD_SERVER_RELOAD_LISTENER_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, _AllDataManager._getInternal(), EventType.TAGS_UPDATED_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, _AllDataManager._getInternal(), EventType.DATAPACK_SYNC_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, LivingShooterSyncHandler.get(), EventType.SERVER_TICK_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, LivingShooterSyncHandler.get(), EventType.ENTITY_JOIN_LEVEL_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, LivingShooterSyncHandler.get(), EventType.PLAYER_CLONE_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, LivingShooterSyncHandler.get(), EventType.PLAYER_START_TRACKING_EVENT, EventPriority.NORMAL, false);
        register(customEventRegister, BulletVictimKnockback.get(), EventType.LIVING_KNOCKBACK_EVENT, EventPriority.NORMAL, false);

        // ----custom event type----
        register(customEventRegister, _WorldImpactHandler.get(), CustomEventType.PROJECTILE_HIT_BLOCK_EVENT, EventPriority.NORMAL, false);
    }

    public static void register(ICustomEventRegister customEventRegister, ICustomEventHandler eventHandler, CustomEventType customEventType, EventPriority priority, boolean receiveCanceled) {
        if (customEventRegister.register(eventHandler, customEventType, priority, receiveCanceled)) {
            CustomGun.LOGGER.debug("{} registered to {}", eventHandler.getEventHandlerName(), customEventType);
        } else {
            CustomGun.LOGGER.debug("Failed to register {} to {}", eventHandler.getEventHandlerName(), customEventType);
        }
    }
    public static void register(ICustomEventRegister customEventRegister, IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (customEventRegister.register(eventHandler, eventType, priority, receiveCanceled)) {
            CustomGun.LOGGER.debug("{} registered to {}", eventHandler.getEventHandlerName(), eventType);
        } else {
            CustomGun.LOGGER.debug("Failed to register {} to {}", eventHandler.getEventHandlerName(), eventType);
        }
    }
}
