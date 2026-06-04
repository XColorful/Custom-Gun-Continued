/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.core.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import xiao.customgun.core.command.ServerCommand;

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
