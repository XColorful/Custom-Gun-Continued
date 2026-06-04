/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforge.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.CommandRegistry;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
public class NeoCommandRegistry {

    private static final CommandRegistry COMMAND_REGISTRY = CommandRegistry.get();

    @SubscribeEvent
    public static void onServerCommandsRegister(RegisterCommandsEvent event) {
        COMMAND_REGISTRY.registerServerCommands(event.getDispatcher());
    }
}
