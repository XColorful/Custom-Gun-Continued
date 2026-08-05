/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.CommonSetup;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
public class NeoCommonSetup {

    private static final CommonSetup COMMON_SETUP = CommonSetup.get();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(COMMON_SETUP::onCommonSetup);
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        COMMON_SETUP.onLoadComplete();
    }
}
