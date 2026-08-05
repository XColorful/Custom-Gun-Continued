/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.core.init;

import com.mojang.brigadier.CommandDispatcher;
import dev.xcolorful.customgun.core.command.ServerCommand;
import net.minecraft.commands.CommandSourceStack;

public class CommandRegistry {

    private static final CommandRegistry INSTANCE = new CommandRegistry();
    public static CommandRegistry get() {
        return INSTANCE;
    }
    private CommandRegistry() {}

    public void registerServerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        ServerCommand.register(dispatcher);
    }
}
