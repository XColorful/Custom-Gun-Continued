/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientTooltipRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoClientTooltipRegistry {

    private static final ClientTooltipRegistry CLIENT_TOOLTIP_REGISTRY = ClientTooltipRegistry.get();

    @SubscribeEvent
    public static void onClientTooltipRegister(RegisterClientTooltipComponentFactoriesEvent event) {
        CLIENT_TOOLTIP_REGISTRY.registerTooltips(event::register);
    }
}
