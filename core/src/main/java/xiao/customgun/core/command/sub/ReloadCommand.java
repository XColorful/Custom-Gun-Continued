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
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.StopWatch;
import xiao.customgun.CustomGun;
import xiao.customgun.client.resource.AllAssetsManager;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.resource.AllDataManager;

import java.util.concurrent.TimeUnit;

import static xiao.customgun.core.command.CommandArg.RELOAD;
import static xiao.customgun.core.command.CommandArg.RELOAD_CLIENT;

public class ReloadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(RELOAD)
                .executes(ReloadCommand::reloadAllPack);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getClient() {
        return Commands.literal(RELOAD_CLIENT)
                .executes(ReloadCommand::reloadClientPack);
    }

    private static int reloadAllPack(CommandContext<CommandSourceStack> context) {
        StopWatch watch = StopWatch.createStarted();
        {
            ISideExecutor sideExecutor = CustomGun.getSideExecutor();
            sideExecutor.unsafeRunWhenOn(McSide.CLIENT, () -> ReloadCommand::reloadClient);
            sideExecutor.safeRunWhenOn(McSide.DEDICATED_SERVER, () -> AllDataManager::reloadAllPack);
        }
        watch.stop();
        double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0;
        context.getSource().sendSystemMessage(Component.translatable("commands.tacz.reload.success", time));
        return Command.SINGLE_SUCCESS;
    }
    private static int reloadClientPack(CommandContext<CommandSourceStack> context) {
        reloadClient();
        return Command.SINGLE_SUCCESS;
    }

    public static void reloadClient() {
        AllAssetsManager.reloadAllPack();
    }
}
