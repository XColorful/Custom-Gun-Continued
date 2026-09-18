package dev.xcolorful.customgun.core.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static dev.xcolorful.customgun.core.command.CommandArg.CONVERT;

public class ConvertCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(CONVERT)
                .executes(ConvertCommand::convert);
    }

    private static int convert(CommandContext<CommandSourceStack> context) {
//        CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> () -> PackConvertor.convert(context.getSource()));
        return Command.SINGLE_SUCCESS;
    }
}