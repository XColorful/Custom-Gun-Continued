package dev.xcolorful.customgun.neoforge.init;


import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.CapabilityRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.capabilities.RegisterCapabilitiesEvent;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoCapabilityRegistry {

    private static final CapabilityRegistry CAPABILITY_REGISTRY = CapabilityRegistry.get();

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        CAPABILITY_REGISTRY.registerCapabilities((name, def) -> event.register(def.clazz()));
    }
}
