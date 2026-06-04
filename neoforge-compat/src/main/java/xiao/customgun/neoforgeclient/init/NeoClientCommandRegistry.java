/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforgeclient.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientCommandRegistry;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NeoClientCommandRegistry {

    private static final ClientCommandRegistry CLIENT_COMMAND_REGISTRY = ClientCommandRegistry.get();

    @SubscribeEvent
    public static void onClientCommandsRegister(RegisterClientCommandsEvent event) {
        CLIENT_COMMAND_REGISTRY.registerClientCommands(event.getDispatcher());
    }
}
