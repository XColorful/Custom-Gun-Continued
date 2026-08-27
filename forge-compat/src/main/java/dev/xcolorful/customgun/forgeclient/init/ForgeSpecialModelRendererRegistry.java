package dev.xcolorful.customgun.forgeclient.init;

import org.jetbrains.annotations.ApiStatus;

/*
为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
 */
@ApiStatus.AvailableSince("1.21.4")
//@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeSpecialModelRendererRegistry {

//    @SubscribeEvent
    public static void onRegisterSpecialModelRenderer(Object event) {
//        event.register(IItemBEWLR.REGISTRY_LOCATION, ForgeBEWLR.BewlrUnbaked.MAP_CODEC);
    }
}
