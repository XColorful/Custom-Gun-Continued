package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

@Deprecated(since = "1.21.6")
@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientRenderRegistry {

//    @SubscribeEvent
//    public static void onRegisterShaders(RegisterShadersEvent event) {
//        ClientRenderRegistry.onRegisterShaders(event::registerShader);
//    }
}
