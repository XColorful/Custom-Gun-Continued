/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientKeyMappingRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientKeyMappingRegistry {

    private static final ClientKeyMappingRegistry CLIENT_KEY_MAPPING_REGISTRY = ClientKeyMappingRegistry.get();

    @SubscribeEvent
    public static void onClientKeyMappingRegister(RegisterKeyMappingsEvent event) {
        CLIENT_KEY_MAPPING_REGISTRY.registerInputCategories(event::registerCategory);
        CLIENT_KEY_MAPPING_REGISTRY.registerKeyMappings(event::register);
    }
}
