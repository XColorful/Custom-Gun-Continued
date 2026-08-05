/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.command.sub._ReloadCommand;
import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.resource._AllDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;

import static dev.xcolorful.customgun.core.command.CommandArg.RELOAD;

public class ReloadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(RELOAD)
                .executes(ReloadCommand::reloadAllPack);
    }

    private static int reloadAllPack(CommandContext<CommandSourceStack> context) {
        StopWatch watch = StopWatch.createStarted();
        {
            ISideExecutor sideExecutor = CustomGun.getSideExecutor();
            sideExecutor.executeOn(McSide.CLIENT, () -> ReloadCommand::reloadClient);
            sideExecutor.executeOnIsolated(McSide.DEDICATED_SERVER, () -> _AllDataManager::reloadAllPack);
        }
        watch.stop();
        double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0;
        context.getSource().sendSystemMessage(Component.translatable("commands.tacz.reload.success", time));
        return Command.SINGLE_SUCCESS;
    }

    public static void reloadClient() {
        _ReloadCommand.reloadClient();
    }
}
