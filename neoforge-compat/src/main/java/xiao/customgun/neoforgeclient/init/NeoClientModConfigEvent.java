/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforgeclient.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientModConfig;
import xiao.customgun.neoforge.CustomGunNeoforge;
import xiao.customgun.neoforge.config.ModConfigTypeHelper;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoClientModConfigEvent {

    public static final ClientModConfig.Event CLIENT_MOD_CONFIG_EVENT = ClientModConfig.Event.get();


    @SubscribeEvent
    public static void onLoadingConfig(ModConfigEvent.Loading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunNeoforge.sideExecutor.getLogicalSide().isClient()) {
            CLIENT_MOD_CONFIG_EVENT.onLoadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }

    @SubscribeEvent
    public static void onReloadingConfig(ModConfigEvent.Reloading event) {
        var config = event.getConfig();
        if (!CustomGun.MOD_ID.equals(config.getModId())) return;

        if (CustomGunNeoforge.sideExecutor.getLogicalSide().isClient()) {
            CLIENT_MOD_CONFIG_EVENT.onReloadingConfig(ModConfigTypeHelper.convert(config.getType()));
        }
    }
}
