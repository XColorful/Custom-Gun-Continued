/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.neoforgeclient.event.events.*;

// ↓加个final只是为了在IDEA左边显眼一点, 枚举写死了也没啥好继承的
public final class NeoClientEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        return switch (eventType) {
            // tick
            case PREPARE_CLIENT_TICK_EVENT -> NeoPrepareClientTickEventManager.register(eventHandler, priority, receiveCanceled);
            case CLIENT_TICK_EVENT -> NeoClientTickEventManager.register(eventHandler, priority, receiveCanceled);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> NeoPrepareClientPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case CLIENT_PLAYER_TICK_EVENT -> NeoClientPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            // input
            case INPUT_KEY_EVENT -> NeoInputKeyEventManager.register(eventHandler, priority, receiveCanceled);
            case INTERACTION_MAPPING_EVENT -> NeoInteractionMappingEventManager.register(eventHandler, priority, receiveCanceled);
            case MOUSE_BUTTON_EVENT -> NeoMouseButtonEventManager.register(eventHandler, priority, receiveCanceled);
            case MOUSE_SCROLLING_EVENT -> NeoMouseScrollingEventManager.register(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> NeoAddClientReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            // render
            case PREPARE_RENDER_FRAME_EVENT -> NeoPrepareRenderFrameEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_FRAME_EVENT -> NeoRenderFrameEventManager.register(eventHandler, priority, receiveCanceled);
            case COMPUTE_CAMERA_ANGLES_EVENT -> NeoComputeCameraAnglesEventManager.register(eventHandler, priority, receiveCanceled);
            case COMPUTE_FOV_EVENT -> NeoComputeFovEventManager.register(eventHandler, priority, receiveCanceled);
            case COMPUTE_FOV_MODIFIER_EVENT -> NeoComputeFovModifierEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_LEVEL_STAGE_EVENT -> NeoRenderLevelStageEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_TRANSLUCENT_EVENT -> NeoRenderTranslucentEventManager.register(eventHandler, priority, receiveCanceled);
            case SUBMIT_CUSTOM_GEOMETRY_EVENT -> NeoSubmitCustomGeometryEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_HAND_EVENT -> NeoRenderHandEventManager.register(eventHandler, priority, receiveCanceled);
            case RENDER_GUI_EVENT -> NeoRenderGuiEventManager.register(eventHandler, priority, receiveCanceled);
            // display
            case ITEM_TOOLTIP_EVENT -> NeoItemTooltipEventManager.register(eventHandler, priority, receiveCanceled);
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
            case PREPARE_CLIENT_TICK_EVENT -> NeoPrepareClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case CLIENT_TICK_EVENT -> NeoClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> NeoPrepareClientPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case CLIENT_PLAYER_TICK_EVENT -> NeoClientPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // input
            case INPUT_KEY_EVENT -> NeoInputKeyEventManager.unregister(eventHandler, priority, receiveCanceled);
            case INTERACTION_MAPPING_EVENT -> NeoInteractionMappingEventManager.unregister(eventHandler, priority, receiveCanceled);
            case MOUSE_BUTTON_EVENT -> NeoMouseButtonEventManager.unregister(eventHandler, priority, receiveCanceled);
            case MOUSE_SCROLLING_EVENT -> NeoMouseScrollingEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> NeoAddClientReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            // render
            case PREPARE_RENDER_FRAME_EVENT -> NeoPrepareRenderFrameEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_FRAME_EVENT -> NeoRenderFrameEventManager.unregister(eventHandler, priority, receiveCanceled);
            case COMPUTE_CAMERA_ANGLES_EVENT -> NeoComputeCameraAnglesEventManager.unregister(eventHandler, priority, receiveCanceled);
            case COMPUTE_FOV_EVENT -> NeoComputeFovEventManager.unregister(eventHandler, priority, receiveCanceled);
            case COMPUTE_FOV_MODIFIER_EVENT -> NeoComputeFovModifierEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_LEVEL_STAGE_EVENT -> NeoRenderLevelStageEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_TRANSLUCENT_EVENT -> NeoRenderTranslucentEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SUBMIT_CUSTOM_GEOMETRY_EVENT -> NeoSubmitCustomGeometryEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_HAND_EVENT -> NeoRenderHandEventManager.unregister(eventHandler, priority, receiveCanceled);
            case RENDER_GUI_EVENT -> NeoRenderGuiEventManager.unregister(eventHandler, priority, receiveCanceled);
            // display
            case ITEM_TOOLTIP_EVENT -> NeoItemTooltipEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }
}
