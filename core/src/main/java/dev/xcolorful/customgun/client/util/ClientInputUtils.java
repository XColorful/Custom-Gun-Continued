/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.jetbrains.annotations.Nullable;

public class ClientInputUtils {

    /**
     * 判断当前是否处于可进行游戏操作的状态
     * @return 当前窗口是否可以接收游戏输入
     */
    public static boolean isGameplayFocused() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (ClientGuiUtils.getOverlay(mc) != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (ClientGuiUtils.getCurrentScreen(mc) != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }

    /**
     * 判断当前是否处于游戏内状态，比{@link #isGameplayFocused()}宽松
     * <br>
     * 不限制: 窗口焦点、鼠标、是否打开GUI
     * @return 当前是否可进行游戏操作
     */
    public static boolean isInGameWorld() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (ClientGuiUtils.getOverlay(mc) != null) {
            return false;
        }
        return true;
    }

    public static class Key {

        public static @Nullable Input getInput(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return null;
            // [1.20.1, 1.21.4)
//          return localPlayer.input;
            // [1.21.4, )
            return localPlayer.input.keyPresses;
        }

        public static boolean forward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.forward();
        }
        public static boolean backward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.backward();
        }
        public static boolean left(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.left();
        }
        public static boolean right(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.right();
        }
        public static boolean jump(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.jump();
        }
        public static boolean shift(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.shift();
        }
        public static boolean sprint(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.sprint();
        }
        public static boolean moving(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.getMoveVector().length() > 0.01f;
        }
        public static boolean movingForward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.hasForwardImpulse();
        }
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true)
    public static boolean isInGame() {
        return isGameplayFocused();
    }
}
