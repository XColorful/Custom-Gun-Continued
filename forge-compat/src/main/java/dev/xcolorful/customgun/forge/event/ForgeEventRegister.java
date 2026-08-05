/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forge.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.forge.event.events.*;
import dev.xcolorful.customgun.forgeclient.CustomGunForgeClient;
import net.minecraftforge.fml.loading.FMLLoader;

// ↓加个final只是为了在IDEA左边显眼一点, 枚举写死了也没啥好继承的
public final class ForgeEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _ForgeClientEventRegister.get().register(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            // tick
            case PREPARE_SERVER_TICK_EVENT -> PrepareServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case SERVER_TICK_EVENT -> ServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case PREPARE_SERVER_PLAYER_TICK_EVENT -> PrepareServerPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case SERVER_PLAYER_TICK_EVENT -> ServerPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            // entity
            case ENTITY_JOIN_LEVEL_EVENT -> EntityJoinLevelEventManager.register(eventHandler, priority, receiveCanceled);
            case ENTITY_TRAVEL_DIMENSION_EVENT -> EntityTravelDimensionEventManager.register(eventHandler, priority, receiveCanceled);
            // living entity
            case LIVING_ATTACK_EVENT -> LivingAttackEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_HURT_EVENT -> LivingHurtEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_DAMAGE_EVENT -> LivingDamageEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_DEATH_EVENT -> LivingDeathEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_HEAL_EVENT -> LivingHealEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_USE_TOTEM_EVENT -> LivingUseTotemEventManager.register(eventHandler, priority, receiveCanceled);
            case LIVING_KNOCKBACK_EVENT -> LivingKnockbackEventManager.register(eventHandler, priority, receiveCanceled);
            // player
            case PLAYER_CLONE_EVENT -> PlayerCloneEventManager.register(eventHandler, priority, receiveCanceled);
            case PLAYER_START_TRACKING_EVENT -> PlayerStartTrackingEventManager.register(eventHandler, priority, receiveCanceled);
            case PLAYER_RESPAWN_EVENT -> PlayerRespawnEventManager.register(eventHandler, priority, receiveCanceled);
            // interact
            case ENTITY_INTERACT_EVENT -> EntityInteractEventManager.register(eventHandler, priority, receiveCanceled);
            case ENTITY_INTERACT_SPECIFIC_EVENT -> EntityInteractSpecificEventManager.register(eventHandler, priority, receiveCanceled);
            case LEFT_CLICK_BLOCK_EVENT -> LeftClickBlockEventManager.register(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_BLOCK_EVENT -> RightClickBlockEventManager.register(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_ITEM_EVENT -> RightClickItemEventManager.register(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> AddServerReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> TagsUpdatedEventManager.register(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> DatapackSyncEventManager.register(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to register handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    @Override
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _ForgeClientEventRegister.get().unregister(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            // tick
            case PREPARE_SERVER_TICK_EVENT -> PrepareServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SERVER_TICK_EVENT -> ServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PREPARE_SERVER_PLAYER_TICK_EVENT -> PrepareServerPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SERVER_PLAYER_TICK_EVENT -> ServerPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // entity
            case ENTITY_JOIN_LEVEL_EVENT -> EntityJoinLevelEventManager.unregister(eventHandler, priority, receiveCanceled);
            case ENTITY_TRAVEL_DIMENSION_EVENT -> EntityTravelDimensionEventManager.unregister(eventHandler, priority, receiveCanceled);
            // living entity
            case LIVING_ATTACK_EVENT -> LivingAttackEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_HURT_EVENT -> LivingHurtEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_DAMAGE_EVENT -> LivingDamageEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_DEATH_EVENT -> LivingDeathEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_HEAL_EVENT -> LivingHealEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_USE_TOTEM_EVENT -> LivingUseTotemEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LIVING_KNOCKBACK_EVENT -> LivingKnockbackEventManager.unregister(eventHandler, priority, receiveCanceled);
            // player
            case PLAYER_CLONE_EVENT -> PlayerCloneEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PLAYER_START_TRACKING_EVENT -> PlayerStartTrackingEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PLAYER_RESPAWN_EVENT -> PlayerRespawnEventManager.unregister(eventHandler, priority, receiveCanceled);
            // interact
            case ENTITY_INTERACT_EVENT -> EntityInteractEventManager.unregister(eventHandler, priority, receiveCanceled);
            case ENTITY_INTERACT_SPECIFIC_EVENT -> EntityInteractSpecificEventManager.unregister(eventHandler, priority, receiveCanceled);
            case LEFT_CLICK_BLOCK_EVENT -> LeftClickBlockEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_BLOCK_EVENT -> RightClickBlockEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RIGHT_CLICK_ITEM_EVENT -> RightClickItemEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> AddServerReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> TagsUpdatedEventManager.unregister(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> DatapackSyncEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    private static class _ForgeClientEventRegister {
        public static IEventRegister get() {
            return CustomGunForgeClient.eventRegister;
        }
    }
}

