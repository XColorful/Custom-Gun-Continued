package dev.xcolorful.customgun.forgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientEntityRendererRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientEntityRendererRegistry {

    private static final ClientEntityRendererRegistry CLIENT_ENTITY_RENDERER_REGISTRY = ClientEntityRendererRegistry.get();

    @SubscribeEvent
    public static void onClientEntityRenderersRegister(EntityRenderersEvent.RegisterRenderers event) {
        CLIENT_ENTITY_RENDERER_REGISTRY.registerEntityRenderers(event::registerEntityRenderer);
    }
}
