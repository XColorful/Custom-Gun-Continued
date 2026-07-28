package xiao.customgun.forgeclient.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientModConfig;
import xiao.customgun.forge.CustomGunForge;
import xiao.customgun.forge.config.ModConfigTypeHelper;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientModConfigEvent {

    public static final ClientModConfig.Event CLIENT_MOD_CONFIG_EVENT = ClientModConfig.Event.get();


    @SubscribeEvent
    public static void onLoadingConfig(ModConfigEvent.Loading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunForge.sideExecutor.getLogicalSide().isClient()) {
            CLIENT_MOD_CONFIG_EVENT.onLoadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }

    @SubscribeEvent
    public static void onReloadingConfig(ModConfigEvent.Reloading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunForge.sideExecutor.getLogicalSide().isClient()) {
            CLIENT_MOD_CONFIG_EVENT.onReloadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }
}
