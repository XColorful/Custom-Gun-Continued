/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.shooter;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.client.api.event.IClientPlayerTickEvent;
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

public final class ReloadKey extends InputKey implements IEventHandler {

    private static final class ReloadKeyHolder {
        private static final ReloadKey INSTANCE = new ReloadKey();
    }

    public static ReloadKey get() {
        return ReloadKeyHolder.INSTANCE;
    }

    private ReloadKey() {
        super(CustomInputKey.RELOAD);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                ClientInputCategory.SHOOTER);
    }
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case INPUT_KEY_EVENT -> onReloadPress((IInputKeyEvent) event);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> autoReload((IClientPlayerTickEvent) event);
        }
    }

    private void onReloadPress(IInputKeyEvent event) {
        // TODO: TaCZ ReloadKey.onReloadPress — InputEvent.Key
    }

    private void autoReload(IClientPlayerTickEvent event) {
        // TODO: TaCZ ReloadKey.autoReload — TickEvent.PlayerTickEvent (CLIENT, START)
    }
}
