/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.player;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;

public final class InteractKey extends InputKey implements IEventHandler {

    private static final class InteractKeyHolder {
        private static final InteractKey INSTANCE = new InteractKey();
    }

    public static InteractKey get() {
        return InteractKeyHolder.INSTANCE;
    }

    private InteractKey() {
        super(CustomInputKey.INTERACT);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                ClientInputCategory.PLAYER);
    }
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case INPUT_KEY_EVENT -> onInteractKeyPress((IInputKeyEvent) event);
            case MOUSE_BUTTON_EVENT -> onInteractMousePress((IMouseButtonEvent) event);
        }
    }

    private void onInteractKeyPress(IInputKeyEvent event) {
        // TODO: TaCZ InteractKey.onInteractKeyPress — InputEvent.Key
    }

    private void onInteractMousePress(IMouseButtonEvent event) {
        // TODO: TaCZ InteractKey.onInteractMousePress — InputEvent.MouseButton.Post
    }
}
