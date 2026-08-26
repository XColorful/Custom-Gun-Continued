/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientSetup {

    private static final ClientSetup INSTANCE = ClientSetup.get();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(INSTANCE::onClientSetup);
    }
}
