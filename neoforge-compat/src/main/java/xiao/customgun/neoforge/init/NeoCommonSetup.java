/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforge.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.CommonSetup;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
public class NeoCommonSetup {

    private static final CommonSetup COMMON_SETUP = CommonSetup.get();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(COMMON_SETUP::onCommonSetup);
    }
}
