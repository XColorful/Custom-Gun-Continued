/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.forgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientModEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeClientModEvent {

    public static ClientModEvent CLIENT_MOD_EVENT = ClientModEvent.get();

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        CLIENT_MOD_EVENT.onClientLoggingIn(event.getPlayer(), event.getConnection());
    }
    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CLIENT_MOD_EVENT.onClientLoggingOut(event.getPlayer(), event.getConnection());
    }
    @SubscribeEvent
    public static void onClientPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        CLIENT_MOD_EVENT.onClientPlayerClone(event.getOldPlayer(), event.getNewPlayer());
    }
}
