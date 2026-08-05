/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.input.shooter;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.CustomGun;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public final class InspectKey extends InputKey {

    private static final class InspectKeyHolder {
        private static final InspectKey INSTANCE = new InspectKey();
    }

    public static InspectKey get() {
        return InspectKeyHolder.INSTANCE;
    }

    private InspectKey() {
        super(CustomInputKey.INSPECT);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, InspectKey.class.getSimpleName());
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
        this.onInspectPress(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onInspectPress(event.getAction());
    }
    private void onInspectPress(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return; // 旁观模式

        ILocalShooterGetter.fromLocalPlayer(player).cgc$inspect();
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onInspectControllerPress(boolean isPress) {
        return false;
    }
}
