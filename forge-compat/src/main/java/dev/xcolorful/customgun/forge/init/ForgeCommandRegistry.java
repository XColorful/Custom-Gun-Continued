/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.forge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.CommandRegistry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommandRegistry {

    private static final CommandRegistry COMMAND_REGISTRY = CommandRegistry.get();

    @SubscribeEvent
    public static void onServerCommandsRegister(RegisterCommandsEvent event) {
        COMMAND_REGISTRY.registerServerCommands(event.getDispatcher());
    }
}
