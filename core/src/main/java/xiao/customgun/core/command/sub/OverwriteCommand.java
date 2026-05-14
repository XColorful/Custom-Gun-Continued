/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static xiao.customgun.core.command.CommandArg.ENABLE;
import static xiao.customgun.core.command.CommandArg.OVERWRITE;

public class OverwriteCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(OVERWRITE)
                .then(Commands.argument(ENABLE, BoolArgumentType.bool())
                        .executes(OverwriteCommand::setOverwrite));
    }

    private static int setOverwrite(CommandContext<CommandSourceStack> context) {
        boolean enable = BoolArgumentType.getBool(context, ENABLE);
        // TODO PreLoadConfig
        if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            // TODO PreLoadConfig check
            if (true) {
                serverPlayer.sendSystemMessage(Component.translatable("commands.tacz.reload.overwrite_off"));
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("commands.tacz.reload.overwrite_on"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}