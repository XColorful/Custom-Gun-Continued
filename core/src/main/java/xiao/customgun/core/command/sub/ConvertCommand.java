/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static xiao.customgun.core.command.CommandArg.CONVERT;

public class ConvertCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(CONVERT)
                .executes(ConvertCommand::convert);
    }

    private static int convert(CommandContext<CommandSourceStack> context) {
//        CustomGun.getSideExecutor().unsafeRunWhenOn(McSide.CLIENT, () -> () -> PackConvertor.convert(context.getSource()));
        return Command.SINGLE_SUCCESS;
    }
}