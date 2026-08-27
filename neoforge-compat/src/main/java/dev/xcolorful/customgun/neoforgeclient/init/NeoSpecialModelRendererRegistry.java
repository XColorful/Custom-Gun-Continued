package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.neoforgeclient.renderer.item.NeoBEWLR;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.jetbrains.annotations.ApiStatus;

/*
// 为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.4")
@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoSpecialModelRendererRegistry {

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderer(RegisterSpecialModelRendererEvent event) {
        event.register(IItemBEWLR.REGISTRY_LOCATION, NeoBEWLR.BewlrUnbaked.MAP_CODEC);
    }
}
