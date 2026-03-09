package xiao.customgun.compat.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import xiao.customgun.CustomGun;
import xiao.customgun.CustomGunClient;
import xiao.customgun.api.common.McSide;

@Mod(CustomGun.MOD_ID)
public class CustomGunNeoforge {

    public CustomGunNeoforge(IEventBus modEventBus) {
        Dist dist = FMLLoader.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CustomGun.init(mcSide);
        if (mcSide == McSide.CLIENT) {
            CustomGunClient.init();
        }

    }
}