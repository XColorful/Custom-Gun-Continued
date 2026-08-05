/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.forge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.ModEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeModEvent {

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
