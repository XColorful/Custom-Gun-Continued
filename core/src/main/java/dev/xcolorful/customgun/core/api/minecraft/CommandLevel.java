/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.minecraft;

import net.minecraft.commands.CommandSourceStack;

import java.util.function.Predicate;

public class CommandLevel {

    /**
     * 手动检查是否有足够权限
     */
    public static boolean hasPermission(CommandSourceStack sourceStack, int level) {
        return sourceStack.hasPermission(level);
    }

    public static Predicate<CommandSourceStack> hasPermission(int level) {
        return source -> hasPermission(source, level);
    }

    /**
     * 1.21.11 改为 net.minecraft.server.permissions.PermissionSet
     */
    public static int permission(int level) {
        return level;
    }
}