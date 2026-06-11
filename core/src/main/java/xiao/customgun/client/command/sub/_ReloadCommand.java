/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import xiao.customgun.client.resource._AllAssetsManager;
import xiao.customgun.core.developer.PlannedRefactor;

import static xiao.customgun.client.command.ClientCommandArg.RELOAD_CLIENT;

public class _ReloadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getClient() {
        return Commands.literal(RELOAD_CLIENT)
                .executes(_ReloadCommand::reloadClientPack);
    }
    private static int reloadClientPack(CommandContext<CommandSourceStack> context) {
        if (PlannedRefactor.TODO_CLIENT_RELOAD) return 0; // TODO 客户端执行会崩溃 (随便切换个资源包就能重载)
        reloadClient();
        return Command.SINGLE_SUCCESS;
    }

    public static void reloadClient() {
        _AllAssetsManager.reloadAllPack();
    }
}
