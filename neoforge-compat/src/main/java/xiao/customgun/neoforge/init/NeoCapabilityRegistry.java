package xiao.customgun.neoforge.init;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.capabilities.RegisterCapabilitiesEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.CapabilityRegistry;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoCapabilityRegistry {

    private static final CapabilityRegistry CAPABILITY_REGISTRY = CapabilityRegistry.get();

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        CAPABILITY_REGISTRY.registerCapabilities((name, def) -> event.register(def.clazz()));
    }
}
