/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import xiao.customgun.core.api.minecraft.CommandLevel;

import static xiao.customgun.core.command.CommandArg.MOD_ID;
import static xiao.customgun.core.command.CommandArg.MOD_ID_SHORT;

public class ServerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(get(MOD_ID));
        dispatcher.register(get(MOD_ID_SHORT));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> get(String rootName) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(rootName);
        root.then(AttachmentLockCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(ConfigCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(ReloadCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(DebugCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(DummyAmmoCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(OverwriteCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(HideTooltipPartCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        root.then(ConvertCommand.get()
                .requires(CommandLevel.hasPermission(2)));
        return root;
    }
}