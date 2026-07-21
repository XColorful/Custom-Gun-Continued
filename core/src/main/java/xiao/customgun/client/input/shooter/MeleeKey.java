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
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.client.util.ClientInputUtils;

public final class MeleeKey extends InputKey {

    private static final class MeleeKeyHolder {
        private static final MeleeKey INSTANCE = new MeleeKey();
    }

    public static MeleeKey get() {
        return MeleeKeyHolder.INSTANCE;
    }

    private MeleeKey() {
        super(CustomInputKey.MELEE);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, MeleeKey.class.getSimpleName());
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
        this.doMeleeLogic(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.doMeleeLogic(event.getAction());
    }
    private void doMeleeLogic(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return; // 旁观模式

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        if (localShooter.cgc$isAim()) return; // 开镜时不能近战

        localShooter.cgc$melee();
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onMeleeControllerPress(boolean isPress) {
        return false;
    }
}
