/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.neoforge.event.events.*;
import dev.xcolorful.customgun.neoforgeclient.CustomGunNeoforgeClient;
import net.neoforged.fml.loading.FMLLoader;

public class NeoEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _NeoClientEventRegister.get().register(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            // tick
            case PREPARE_SERVER_TICK_EVENT -> NeoPrepareServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case SERVER_TICK_EVENT -> NeoServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case PREPARE_SERVER_PLAYER_TICK_EVENT -> NeoPrepareServerPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case SERVER_PLAYER_TICK_EVENT -> NeoServerPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            // entity
            case ENTITY_JOIN_LEVEL_EVENT -> NeoEntityJoinLevelEventManager.register(eventHandler, priority, receiveCanceled);
            case ENTITY_TRAVEL_DIMENSION_EVENT -> NeoEntityTravelDimensionEventManager.register(eventHandler, priority, receiveCanceled);
            // living entity
            case LIVING_ATTACK_EVENT -> NeoLivingAttackEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_HURT_EVENT -> NeoLivingHurtEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_DAMAGE_EVENT -> NeoLivingDamageEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_DEATH_EVENT -> NeoLivingDeathEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_HEAL_EVENT -> NeoLivingHealEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_USE_TOTEM_EVENT -> NeoLivingUseTotemEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_KNOCKBACK_EVENT -> NeoLivingKnockbackEventManager.register(eventHandler, priority, receiveCanceled);
            // player
            case PLAYER_CLONE_EVENT -> NeoPlayerCloneEventManager.register(eventHandler, priority, receiveCanceled);
            case PLAYER_START_TRACKING_EVENT -> NeoPlayerStartTrackingEventManager.register(eventHandler, priority, receiveCanceled);
            case PLAYER_RESPAWN_EVENT -> NeoPlayerRespawnEventManager.register(eventHandler, priority, receiveCanceled);
            // interact
            case ENTITY_INTERACT_EVENT -> NeoEntityInteractEventManager.register(eventHandler, priority, receiveCanceled);
            case ENTITY_INTERACT_SPECIFIC_EVENT -> NeoEntityInteractSpecificEventManager.register(eventHandler, priority, receiveCanceled);
            case LEFT_CLICK_BLOCK_EVENT -> NeoLeftClickBlockEventManager.register(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_BLOCK_EVENT -> NeoRightClickBlockEventManager.register(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_ITEM_EVENT -> NeoRightClickItemEventManager.register(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> NeoAddServerReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> NeoTagsUpdatedEventManager.register(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> NeoDatapackSyncEventManager.register(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to register handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    @Override
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _NeoClientEventRegister.get().unregister(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            // tick
            case PREPARE_SERVER_TICK_EVENT -> NeoPrepareServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SERVER_TICK_EVENT -> NeoServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PREPARE_SERVER_PLAYER_TICK_EVENT -> NeoPrepareServerPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SERVER_PLAYER_TICK_EVENT -> NeoServerPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // entity
            case ENTITY_JOIN_LEVEL_EVENT -> NeoEntityJoinLevelEventManager.unregister(eventHandler, priority, receiveCanceled);
            case ENTITY_TRAVEL_DIMENSION_EVENT -> NeoEntityTravelDimensionEventManager.unregister(eventHandler, priority, receiveCanceled);
            // living entity
            case LIVING_ATTACK_EVENT -> NeoLivingAttackEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_HURT_EVENT -> NeoLivingHurtEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_DAMAGE_EVENT -> NeoLivingDamageEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_DEATH_EVENT -> NeoLivingDeathEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_HEAL_EVENT -> NeoLivingHealEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_USE_TOTEM_EVENT -> NeoLivingUseTotemEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_KNOCKBACK_EVENT -> NeoLivingKnockbackEventManager.unregister(eventHandler, priority, receiveCanceled);
            // player
            case PLAYER_CLONE_EVENT -> NeoPlayerCloneEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PLAYER_START_TRACKING_EVENT -> NeoPlayerStartTrackingEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PLAYER_RESPAWN_EVENT -> NeoPlayerRespawnEventManager.unregister(eventHandler, priority, receiveCanceled);
            // interact
            case ENTITY_INTERACT_EVENT -> NeoEntityInteractEventManager.unregister(eventHandler, priority, receiveCanceled);
            case ENTITY_INTERACT_SPECIFIC_EVENT -> NeoEntityInteractSpecificEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LEFT_CLICK_BLOCK_EVENT -> NeoLeftClickBlockEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_BLOCK_EVENT -> NeoRightClickBlockEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_ITEM_EVENT -> NeoRightClickItemEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> NeoAddServerReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> NeoTagsUpdatedEventManager.unregister(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> NeoDatapackSyncEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    private static class _NeoClientEventRegister {
        public static IEventRegister get() {
            return CustomGunNeoforgeClient.eventRegister;
        }
    }
}
