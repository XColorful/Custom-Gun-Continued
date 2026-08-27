package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.4")
//@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoSpecialModelRendererRegistry {

    public static final ResourceLocation REGISTRY_LOCATION = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "item_bewlr"));

    //    @SubscribeEvent
    public static void onRegisterSpecialModelRenderer(Object event) {
//        event.register(REGISTRY_LOCATION, NeoBEWLR.BewlrUnbaked.MAP_CODEC);
    }
}
