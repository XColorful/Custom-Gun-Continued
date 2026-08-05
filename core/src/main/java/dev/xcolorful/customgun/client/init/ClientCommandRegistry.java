/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import com.mojang.brigadier.CommandDispatcher;
import dev.xcolorful.customgun.client.command.ClientCommand;
import net.minecraft.commands.CommandSourceStack;

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
