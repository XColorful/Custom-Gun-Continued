package dev.xcolorful.customgun.forge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.CapabilityRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeCapabilityRegistry {

    private static final CapabilityRegistry CAPABILITY_REGISTRY = CapabilityRegistry.get();

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        CAPABILITY_REGISTRY.registerCapabilities((name, def) -> event.register(def.clazz()));
    }
}
