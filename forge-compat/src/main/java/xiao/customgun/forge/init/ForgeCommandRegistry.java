/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.forge.init;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.CommandRegistry;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommandRegistry {

    private static final CommandRegistry COMMAND_REGISTRY = CommandRegistry.get();

    @SubscribeEvent
    public static void onServerCommandsRegister(RegisterCommandsEvent event) {
        COMMAND_REGISTRY.registerServerCommands(event.getDispatcher());
    }
}
