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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.CustomGun;
import xiao.customgun.client.CustomGunClient;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.config.sync.InteractFilterData;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.client.util.ClientInputUtils;
import xiao.customgun.core.api.item.gun.IGunGetter;

public final class InteractKey extends InputKey {

    private static final class InteractKeyHolder {
        private static final InteractKey INSTANCE = new InteractKey();
    }

    public static InteractKey get() {
        return InteractKeyHolder.INSTANCE;
    }

    private InteractKey() {
        super(CustomInputKey.INTERACT);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                ClientInputCategory.PLAYER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, InteractKey.class.getSimpleName());
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
        this.onInteractKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onInteractKeyInput(event.getAction());
    }
    private void onInteractKeyInput(int action) {
        if (action != GLFW.GLFW_PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (IGunGetter.fromMainHand(player) == null // 主手没枪
                || player.isSpectator() // 旁观模式
        ) return;

        HitResult hitResult = mc.hitResult;
        if (hitResult == null) return;

        // 方块交互
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = player.level().getBlockState(blockPos);
            if (InteractFilterData.canInteract(blockState)) {
                CustomGunClient.getAccessTransformer().startUseItem(mc);
            }
        }
        // 实体交互
        else if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (InteractFilterData.canInteract(entity)) {
                CustomGunClient.getAccessTransformer().startUseItem(mc);
            }
        }
    }
}
