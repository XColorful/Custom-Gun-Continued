package dev.xcolorful.customgun.neoforgeclient.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientRenderRegistry {

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        ClientRenderRegistry.onRegisterRenderPipelines(event::registerPipeline);
    }
}
