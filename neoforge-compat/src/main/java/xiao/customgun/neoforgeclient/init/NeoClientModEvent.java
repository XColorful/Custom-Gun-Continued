/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforgeclient.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientModEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NeoClientModEvent {

    public static ClientModEvent CLIENT_MOD_EVENT = ClientModEvent.get();

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        CLIENT_MOD_EVENT.onClientLoggingIn(event.getPlayer(), event.getConnection());
    }
    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CLIENT_MOD_EVENT.onClientLoggingOut(event.getPlayer(), event.getConnection());
    }
}
