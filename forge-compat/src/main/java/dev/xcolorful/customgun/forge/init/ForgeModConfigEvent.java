package dev.xcolorful.customgun.forge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.ModConfig;
import dev.xcolorful.customgun.forge.CustomGunForge;
import dev.xcolorful.customgun.forge.config.ModConfigTypeHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeModConfigEvent {

    public static final ModConfig.Event MOD_CONFIG_EVENT = ModConfig.Event.get();

    @SubscribeEvent
    public static void onLoadingConfig(ModConfigEvent.Loading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunForge.sideExecutor.getLogicalSide().isServer()) {
            MOD_CONFIG_EVENT.onLoadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }

    @SubscribeEvent
    public static void onReloadingConfig(ModConfigEvent.Reloading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunForge.sideExecutor.getLogicalSide().isServer()) {
            MOD_CONFIG_EVENT.onReloadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }
}
