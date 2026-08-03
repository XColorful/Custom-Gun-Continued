/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.shooter;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.ILocalShooter;
import xiao.customgun.client.api.entity.shooter.ILocalShooterGetter;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.event.IPrepareClientTickEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.config.KeyConfig;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.client.util.ClientInputUtils;
import xiao.customgun.core.api.event.*;

public final class ProneKey extends InputKey implements IEventHandler {

    private static final class ProneKeyHolder {
        private static final ProneKey INSTANCE = new ProneKey();
    }

    public static ProneKey get() {
        return ProneKeyHolder.INSTANCE;
    }

    private ProneKey() {
        super(CustomInputKey.PRONE);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProneKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.PREPARE_CLIENT_TICK_EVENT) {
            onProneHoldingPreInput((IPrepareClientTickEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onPronePress(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onPronePress(event.getAction());
    }
    private void onPronePress(int action) {
        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        boolean holdToProne = KeyConfig.HOLD_TO_PRONE.get();

        switch (action) {
            case GLFW.GLFW_PRESS -> {
                localShooter.cgc$prone(holdToProne || !localShooter.cgc$isProne());
            }
            case GLFW.GLFW_RELEASE -> {
                if (holdToProne) localShooter.cgc$prone(false);
            }
        }
    }

    /**
     * 按住趴下模式:
     * 1. 不在窗口焦点就取消趴姿
     * 2. 按住但没在趴姿时, 切换到趴姿
     */
    private void onProneHoldingPreInput(IPrepareClientTickEvent event) {
        boolean holdToProne = KeyConfig.HOLD_TO_PRONE.get();
        if  (!holdToProne) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);

        boolean isProne = ClientInputUtils.isGameplayFocused() // 在焦点
                && this.keyMapping.get().isDown(); // 按住了趴键
        if (localShooter.cgc$isProne() != isProne) {
            localShooter.cgc$prone(isProne);
        }
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onProneControllerPress(boolean isPress) {
        return false;
    }
}
