package xiao.customgun.neoforge.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.ModConfig;
import xiao.customgun.neoforge.CustomGunNeoforge;
import xiao.customgun.neoforge.config.ModConfigTypeHelper;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoModConfigEvent {

    public static final ModConfig.Event MOD_CONFIG_EVENT = ModConfig.Event.get();

    @SubscribeEvent
    public static void onLoadingConfig(ModConfigEvent.Loading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunNeoforge.sideExecutor.getLogicalSide().isServer()) {
            MOD_CONFIG_EVENT.onLoadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }

    @SubscribeEvent
    public static void onReloadingConfig(ModConfigEvent.Reloading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunNeoforge.sideExecutor.getLogicalSide().isServer()) {
            MOD_CONFIG_EVENT.onReloadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }
}
