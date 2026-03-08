package xiao.customgun.compat.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import xiao.customgun.CustomGun;
import xiao.customgun.CustomGunClient;
import xiao.customgun.api.common.McSide;

@Mod(CustomGun.MOD_ID)
public class CustomGunForge {

    public CustomGunForge() {
        Dist dist = FMLLoader.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CustomGun.init(mcSide);
        if (mcSide == McSide.CLIENT) {
            CustomGunClient.init();
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }
}