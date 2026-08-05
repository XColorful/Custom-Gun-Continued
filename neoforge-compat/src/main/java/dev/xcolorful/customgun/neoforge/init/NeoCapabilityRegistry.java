package dev.xcolorful.customgun.neoforge.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import dev.xcolorful.customgun.core.init.CapabilityRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NeoCapabilityRegistry {

    private static final CapabilityRegistry CAPABILITY_REGISTRY = CapabilityRegistry.get();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CustomGun.MOD_ID);
    public static Supplier<AttachmentType<SyncDataHolder>> SYNC_DATA_HOLDER;
    @SuppressWarnings("unchecked")
    public static void onRegisterCapabilities(IEventBus modEventBus) {
        CAPABILITY_REGISTRY.registerCapabilities((name, def) -> {
            SYNC_DATA_HOLDER = ATTACHMENT_TYPES.register(name,
                    () -> AttachmentType.builder((Supplier<SyncDataHolder>) def.factory()).build());
        });
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
