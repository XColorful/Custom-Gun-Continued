/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.ModEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
public class NeoModEvent {

    public static ModEvent MOD_EVENT = ModEvent.get();

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MOD_EVENT.onServerStarting(event.getServer());
    }
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MOD_EVENT.onServerStopping(event.getServer());
    }
}
