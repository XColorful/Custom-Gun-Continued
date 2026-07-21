/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.util;

import net.minecraft.client.Minecraft;

public class ClientInputUtils {

    /**
     * 判断当前是否处于可进行游戏操作的状态
     * @return 当前窗口是否可以接收游戏输入
     */
    public static boolean isGameplayFocused() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.getOverlay() != null) {
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

    // --------Deprecated--------

    @Deprecated(forRemoval = true)
    public static boolean isInGame() {
        return isGameplayFocused();
    }
}
