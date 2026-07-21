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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ShootKey extends InputKey implements IEventHandler {

    private static final class ShootKeyHolder {
        private static final ShootKey INSTANCE = new ShootKey();
    }

    public static ShootKey get() {
        return ShootKeyHolder.INSTANCE;
    }

    private ShootKey() {
        super(CustomInputKey.SHOOT);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ShootKey.class.getSimpleName());
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
        this.inputQueue.clear();
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case PREPARE_CLIENT_TICK_EVENT -> onShootPreInput((IPrepareClientTickEvent) event);
            case CLIENT_TICK_EVENT -> checkShoot((IClientTickEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onShootKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onShootKeyInput(event.getAction());
    }
    private void onShootKeyInput(int action) {
        // TODO 添加到操作队列, 在preTick和tick消耗掉
    }

    private final Queue<?> inputQueue = new ConcurrentLinkedQueue<>();

    private void onShootPreInput(IPrepareClientTickEvent event) {
    }
    private void checkShoot(IClientTickEvent event) {
        boolean doShoot = false;
//        if (!ClientInputUtils.isGameplayFocused()) return;
//
//        LocalPlayer player = Minecraft.getInstance().player;
//        if (player == null || player.isSpectator()) return;
//
//        if (IGunGetter.fromMainHand(player) == null) return;
//
//        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
//        boolean isShootDown = this.keyMapping.get().isDown();
//
//        localShooter.cgc$chargeShoot(isShootDown);
//        if (isShootDown) {
//            localShooter.cgc$localShoot();
//        }
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean shootControllerTick(boolean isShootDown) {
        return false;
    }
}
