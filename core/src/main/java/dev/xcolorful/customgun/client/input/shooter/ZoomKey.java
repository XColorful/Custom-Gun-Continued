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
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.input.IInputKeyManager;
import dev.xcolorful.customgun.client.api.input.IKeyConflictContext;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.input.IKeyModifier;
import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputKey;
import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.InputKey;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.network.message.ClientMessagePlayerZoom;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public final class ZoomKey extends InputKey {

    private static final class ZoomKeyHolder {
        private static final ZoomKey INSTANCE = new ZoomKey();
    }

    public static ZoomKey get() {
        return ZoomKeyHolder.INSTANCE;
    }

    private ZoomKey() {
        super(CustomInputKey.ZOOM);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ZoomKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        return true;
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onZoomKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onZoomKeyInput(event.getAction());
    }
    private void onZoomKeyInput(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (IGunGetter.fromMainHand(player) == null // 主手没枪
                || player.isSpectator() // 旁观模式
        ) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        if (!localShooter.cgc$isAim()) return; // 不在瞄准

        SendUtils.sendMessageToServer(new ClientMessagePlayerZoom());
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onZoomControllerPress(boolean isPress) {
        return false;
    }
}
