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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.ILocalShooter;
import xiao.customgun.client.api.entity.shooter.ILocalShooterGetter;
import xiao.customgun.client.api.event.IClientPlayerTickEvent;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
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
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

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

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ReloadKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> autoReload((IClientPlayerTickEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onReloadKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onReloadKeyInput(event.getAction());
    }
    private void onReloadKeyInput(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return; // 旁观模式

        ItemStack gunItem = player.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return; // 主手没枪

        if (iGun.useInventoryAmmo(gunItem)) return; // 背包直读 -> 不需要装弹

        ILocalShooterGetter.fromLocalPlayer(player).cgc$reload();
    }

    public static int AUTO_RELOAD_FREQUENCY = 5;
    private void autoReload(IClientPlayerTickEvent event) {
        if (!KeyConfig.AUTO_RELOAD.get()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() // 旁观模式
                || player.tickCount % AUTO_RELOAD_FREQUENCY != 0) return;

        ItemStack gunItem = player.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return; // 主手没枪

        if (iGun.useInventoryAmmo(gunItem)) return; // 背包直读 -> 不需要装弹

        { // 枪管有子弹就不装弹
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance == null) return;

            BoltType boltType = gunIndexInstance.getGunData().getBoltType();
            boolean hasBarrelAmmo = boltType == BoltType.OPEN_BOLT ? iGun.getMagAmmoCountWithBarrel(gunItem, boltType) > 0
                    : iGun.hasBarrelAmmo(gunItem);
            if (hasBarrelAmmo) return;
        }

        ILocalShooterGetter.fromLocalPlayer(player).cgc$reload();
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onReloadControllerPress(boolean isPress) {
        return false;
    }
}
