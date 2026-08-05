/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.forgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientTooltipRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientTooltipRegistry {

    private static final ClientTooltipRegistry CLIENT_TOOLTIP_REGISTRY = ClientTooltipRegistry.get();

    @SubscribeEvent
    public static void onClientTooltipRegister(RegisterClientTooltipComponentFactoriesEvent event) {
        CLIENT_TOOLTIP_REGISTRY.registerTooltips(event::register);
    }
}
