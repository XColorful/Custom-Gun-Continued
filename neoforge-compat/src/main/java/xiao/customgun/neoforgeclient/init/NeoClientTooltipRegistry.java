/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforgeclient.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientTooltipRegistry;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientTooltipRegistry {

    private static final ClientTooltipRegistry CLIENT_TOOLTIP_REGISTRY = ClientTooltipRegistry.get();

    @SubscribeEvent
    public static void onClientTooltipRegister(RegisterClientTooltipComponentFactoriesEvent event) {
        CLIENT_TOOLTIP_REGISTRY.registerTooltips(event::register);
    }
}
