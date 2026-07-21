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
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;

public final class SwitchFireModeKey extends InputKey {

    private static final class SwitchFireModeKeyHolder {
        private static final SwitchFireModeKey INSTANCE = new SwitchFireModeKey();
    }

    public static SwitchFireModeKey get() {
        return SwitchFireModeKeyHolder.INSTANCE;
    }

    private SwitchFireModeKey() {
        super(CustomInputKey.SWITCH_FIRE_MODE);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, SwitchFireModeKey.class.getSimpleName());
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
        this.onFireSelectKeyPress(event);
    }

    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onFireSelectMousePress(event);
    }

    private void onFireSelectKeyPress(IInputKeyEvent event) {
        // TODO: TaCZ FireSelectKey.onFireSelectKeyPress — InputEvent.Key
    }

    private void onFireSelectMousePress(IMouseButtonEvent event) {
        // TODO: TaCZ FireSelectKey.onFireSelectMousePress — InputEvent.MouseButton.Post
    }
}
