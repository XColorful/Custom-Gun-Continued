/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.shooter;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IClientTickEvent;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.event.IPrepareClientTickEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.core.api.event.*;

public final class AimKey extends InputKey implements IEventHandler {

    private static final class AimKeyHolder {
        private static final AimKey INSTANCE = new AimKey();
    }

    public static AimKey get() {
        return AimKeyHolder.INSTANCE;
    }

    private AimKey() {
        super(CustomInputKey.AIM);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, AimKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        customEventRegister.register(this, EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        customEventRegister.unregister(this, EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case PREPARE_CLIENT_TICK_EVENT -> onAimHoldingPreInput((IPrepareClientTickEvent) event);
            case CLIENT_TICK_EVENT -> cancelAim((IClientTickEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
    }

    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onAimPress(event);
    }

    private void onAimPress(IMouseButtonEvent event) {
        // TODO: TaCZ AimKey.onAimPress — InputEvent.MouseButton.Post
    }

    private void onAimHoldingPreInput(IPrepareClientTickEvent event) {
        // TODO: TaCZ AimKey.onAimHoldingPreInput — TickEvent.ClientTickEvent (PRE)
    }

    private void cancelAim(IClientTickEvent event) {
        // TODO: TaCZ AimKey.cancelAim — TickEvent.ClientTickEvent (END)
    }
}
