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

import static xiao.customgun.client.command.ClientCommandArg.RELOAD_CLIENT;

public class _ReloadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getClient() {
        return Commands.literal(RELOAD_CLIENT)
                .executes(_ReloadCommand::reloadClientPack);
    }
    private static int reloadClientPack(CommandContext<CommandSourceStack> context) {
        _AllAssetsManager.reloadClientPack();
        return Command.SINGLE_SUCCESS;
    }

    public static void reloadClient() {
        _AllAssetsManager.reloadAllPack();
    }
}
