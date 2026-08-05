/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.input.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.CustomGun;
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

/**
 * 已移至{@code dev.xcolorful.cgcconfig.client.input.config.ConfigKey}
 */
@Deprecated(forRemoval = true)
public final class ConfigKey extends InputKey {

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

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ConfigKey.class.getSimpleName());
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
        this.onConfigKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onConfigKeyInput(event.getAction());
    }
    private void onConfigKeyInput(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
    }
}
