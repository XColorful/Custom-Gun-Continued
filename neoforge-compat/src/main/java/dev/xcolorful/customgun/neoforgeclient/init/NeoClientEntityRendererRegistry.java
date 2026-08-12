package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientEntityRendererRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoClientEntityRendererRegistry {

    private static final ClientEntityRendererRegistry CLIENT_ENTITY_RENDERER_REGISTRY = ClientEntityRendererRegistry.get();

    @SubscribeEvent
    public static void onClientEntityRenderersRegister(EntityRenderersEvent.RegisterRenderers event) {
        CLIENT_ENTITY_RENDERER_REGISTRY.registerEntityRenderers(event::registerEntityRenderer);
    }
}
