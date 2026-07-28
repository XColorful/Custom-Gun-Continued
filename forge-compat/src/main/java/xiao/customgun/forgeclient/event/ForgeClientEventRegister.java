/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forgeclient.event;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.forgeclient.event.events.*;

// ↓加个final只是为了在IDEA左边显眼一点, 枚举写死了也没啥好继承的
public final class ForgeClientEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        return switch (eventType) {
            // tick
            case PREPARE_CLIENT_TICK_EVENT -> PrepareClientTickEventManager.register(eventHandler, priority, receiveCanceled);
            case CLIENT_TICK_EVENT -> ClientTickEventManager.register(eventHandler, priority, receiveCanceled);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> PrepareClientPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case CLIENT_PLAYER_TICK_EVENT -> ClientPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            // input
            case INPUT_KEY_EVENT -> InputKeyEventManager.register(eventHandler, priority, receiveCanceled);
            case INTERACTION_MAPPING_EVENT -> InteractionMappingEventManager.register(eventHandler, priority, receiveCanceled);
            case MOUSE_BUTTON_EVENT -> MouseButtonEventManager.register(eventHandler, priority, receiveCanceled);
            case MOUSE_SCROLLING_EVENT -> MouseScrollingEventManager.register(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> AddClientReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            // render
            case RENDER_LEVEL_STAGE_EVENT -> RenderLevelStageEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_TRANSLUCENT_EVENT -> RenderTranslucentEventManager.register(eventHandler, priority, receiveCanceled);
            case SUBMIT_CUSTOM_GEOMETRY_EVENT -> SubmitCustomGeometryEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_GUI_EVENT -> RenderGuiEventManager.register(eventHandler, priority, receiveCanceled);
            // display
            case ITEM_TOOLTIP_EVENT -> ItemTooltipEventManager.register(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to register handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    @Override
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        return switch (eventType) {
            // tick
            case PREPARE_CLIENT_TICK_EVENT -> PrepareClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case CLIENT_TICK_EVENT -> ClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> PrepareClientPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case CLIENT_PLAYER_TICK_EVENT -> ClientPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // input
            case INPUT_KEY_EVENT -> InputKeyEventManager.unregister(eventHandler, priority, receiveCanceled);
            case INTERACTION_MAPPING_EVENT -> InteractionMappingEventManager.unregister(eventHandler, priority, receiveCanceled);
            case MOUSE_BUTTON_EVENT -> MouseButtonEventManager.unregister(eventHandler, priority, receiveCanceled);
            case MOUSE_SCROLLING_EVENT -> MouseScrollingEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> AddClientReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            // render
            case RENDER_LEVEL_STAGE_EVENT -> RenderLevelStageEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_TRANSLUCENT_EVENT -> RenderTranslucentEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SUBMIT_CUSTOM_GEOMETRY_EVENT -> SubmitCustomGeometryEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_GUI_EVENT -> RenderGuiEventManager.unregister(eventHandler, priority, receiveCanceled);
            // display
            case ITEM_TOOLTIP_EVENT -> ItemTooltipEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }
}
