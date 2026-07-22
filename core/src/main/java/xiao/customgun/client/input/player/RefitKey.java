/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.player;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.gui.GunRefitScreen;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.client.util.ClientGuiUtils;
import xiao.customgun.client.util.ClientInputUtils;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

public final class RefitKey extends InputKey {

    private static final class RefitKeyHolder {
        private static final RefitKey INSTANCE = new RefitKey();
    }

    public static RefitKey get() {
        return RefitKeyHolder.INSTANCE;
    }

    private RefitKey() {
        super(CustomInputKey.REFIT);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                ClientInputCategory.PLAYER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, RefitKey.class.getSimpleName());
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
        this.onRefitKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onRefitKeyInput(event.getAction());
    }
    private void onRefitKeyInput(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() // 旁观模式
        ) return;

        if (ClientInputUtils.isGameplayFocused()) {
            ItemStack gunItem = player.getMainHandItem();
            IGun iGun = IGunGetter.fromItemStack(gunItem);
            if (iGun == null || ClientGuiUtils.getCurrentScreen(mc) == null) return;
            if (iGun.hasAttachmentLock(gunItem)) return;

            ClientGuiUtils.setCurrentScreen(mc, new GunRefitScreen());
        } else if (ClientGuiUtils.getCurrentScreen(mc) instanceof GunRefitScreen screen) {
            screen.onClose();
        }
    }
}
