/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.client.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import xiao.customgun.client.command.ClientCommand;

public class ClientCommandRegistry {

    private static final ClientCommandRegistry INSTANCE = new ClientCommandRegistry();
    public static ClientCommandRegistry get() {
        return INSTANCE;
    }
    private ClientCommandRegistry() {}

    public void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        ClientCommand.register(dispatcher);
    }
}
