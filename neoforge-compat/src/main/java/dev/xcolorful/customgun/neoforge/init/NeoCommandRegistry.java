/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.CommandRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
public class NeoCommandRegistry {

    private static final CommandRegistry COMMAND_REGISTRY = CommandRegistry.get();

    @SubscribeEvent
    public static void onServerCommandsRegister(RegisterCommandsEvent event) {
        COMMAND_REGISTRY.registerServerCommands(event.getDispatcher());
    }
}
