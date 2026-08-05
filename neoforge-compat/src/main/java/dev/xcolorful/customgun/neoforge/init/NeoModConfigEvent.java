package dev.xcolorful.customgun.neoforge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.ModConfig;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import dev.xcolorful.customgun.neoforge.config.ModConfigTypeHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
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
