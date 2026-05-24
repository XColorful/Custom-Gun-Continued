/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforge.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.CommonSetup;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
