/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.forgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientCommandRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeClientCommandRegistry {

    private static final ClientCommandRegistry CLIENT_COMMAND_REGISTRY = ClientCommandRegistry.get();

    @SubscribeEvent
    public static void onClientCommandsRegister(RegisterClientCommandsEvent event) {
        CLIENT_COMMAND_REGISTRY.registerClientCommands(event.getDispatcher());
    }
}
