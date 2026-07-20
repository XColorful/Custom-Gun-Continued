/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.config;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;

public final class ConfigKey extends InputKey implements IEventHandler {

    private static final class ConfigKeyHolder {
        private static final ConfigKey INSTANCE = new ConfigKey();
    }

    public static ConfigKey get() {
        return ConfigKeyHolder.INSTANCE;
    }

    private ConfigKey() {
        super(CustomInputKey.CONFIG);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.ALT,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_T,
                ClientInputCategory.CONFIG);
    }
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case INPUT_KEY_EVENT -> onOpenConfig((IInputKeyEvent) event);
        }
    }

    private void onOpenConfig(IInputKeyEvent event) {
        // TODO: TaCZ ConfigKey.onOpenConfig — InputEvent.Key
    }
}
