package dev.xcolorful.customgun.client.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xcolorful.customgun.client.resource._AllAssetsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static dev.xcolorful.customgun.client.command.ClientCommandArg.RELOAD_CLIENT;

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
