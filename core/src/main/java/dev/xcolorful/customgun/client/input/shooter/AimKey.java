/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.input.shooter;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IClientTickEvent;
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.event.IPrepareClientTickEvent;
import dev.xcolorful.customgun.client.api.input.IInputKeyManager;
import dev.xcolorful.customgun.client.api.input.IKeyConflictContext;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.input.IKeyModifier;
import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputKey;
import dev.xcolorful.customgun.client.config.KeyConfig;
import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.InputKey;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

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
            case CLIENT_TICK_EVENT -> checkAim((IClientTickEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onAimKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onAimKeyInput(event.getAction());
    }
    private void onAimKeyInput(int action) {
        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (IGunGetter.fromMainHand(player) == null // 主手没枪
                || player.isSpectator() // 旁观模式
        ) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        boolean holdToAim = KeyConfig.HOLD_TO_AIM.get();

        switch (action) {
            case GLFW.GLFW_PRESS -> {
                localShooter.cgc$aim(holdToAim || !localShooter.cgc$isAim());
            }
            case GLFW.GLFW_RELEASE -> {
                if (holdToAim) localShooter.cgc$aim(false);
            }
        }
    }

    /**
     * 按住瞄准模式:
     * 1. 不在窗口焦点就取消瞄准
     * 2. 按住但没在瞄准状态时, 切换到瞄准状态
     */
    private void onAimHoldingPreInput(IPrepareClientTickEvent event) {
        boolean holdToAim = KeyConfig.HOLD_TO_AIM.get();
        if (!holdToAim) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        if (IGunGetter.fromMainHand(player) == null) {
            localShooter.cgc$aim(false);
            return;
        }

        boolean isAim = ClientInputUtils.isGameplayFocused() // 在焦点
                && this.keyMapping.get().isDown(); // 按住了瞄准
        if (localShooter.cgc$isAim() != isAim) {
            localShooter.cgc$aim(isAim);
        }
    }
    /**
     * tick结束时的校正 (仅用于取消瞄准)
     */
    private void checkAim(IClientTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        if (!ClientInputUtils.isGameplayFocused() // 不在焦点
                || IGunGetter.fromMainHand(player) == null // 主手没枪
                || player.isSpectator() // 旁观模式
        ) {
            localShooter.cgc$aim(false);
        }
    }

    // --------Deprecated--------

    /**
     * TODO Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onAimControllerPress(boolean isPress) {
        return false;
    }
}
