package dev.xcolorful.customgun.neoforge;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrarFactory;
import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.INetworkHook;
import dev.xcolorful.customgun.core.init.registry.*;
import dev.xcolorful.customgun.core.network.NetworkHandler;
import dev.xcolorful.customgun.neoforge.common.NeoSideExecutor;
import dev.xcolorful.customgun.neoforge.config.NeoModConfigSpecBuilder;
import dev.xcolorful.customgun.neoforge.event.NeoEventRegister;
import dev.xcolorful.customgun.neoforge.init.NeoCapabilityRegistry;
import dev.xcolorful.customgun.neoforge.init.registry.NeoRegistrarFactory;
import dev.xcolorful.customgun.neoforge.minecraft.NeoCapabilityProvider;
import dev.xcolorful.customgun.neoforge.minecraft.NeoRegistry;
import dev.xcolorful.customgun.neoforge.minecraft.access.NeoAccessTransformer;
import dev.xcolorful.customgun.neoforge.network.NeoNetworkAdapter;
import dev.xcolorful.customgun.neoforge.network.NeoNetworkHook;
import dev.xcolorful.customgun.neoforgeclient.CustomGunNeoforgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@Mod(CustomGun.MOD_ID)
public class CustomGunNeoforge {

    public static ISideExecutor sideExecutor = NeoSideExecutor.INSTANCE;
    public static IRegistrarFactory registrarFactory = NeoRegistrarFactory.INSTANCE;
    public static IMcRegistry mcRegistry = NeoRegistry.INSTANCE;
    public static ICapabilityProvider capabilityProvider = NeoCapabilityProvider.INSTANCE;
    public static INetworkAdapter networkAdapter;
    public static INetworkHook networkHook = NeoNetworkHook.INSTANCE;
    public static IEventRegister eventRegister = NeoEventRegister.INSTANCE;
    public static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier = NeoModConfigSpecBuilder::new;
    public static NeoAccessTransformer accessTransformer = NeoAccessTransformer.INSTANCE;

    public CustomGunNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        CustomGunNeoforge.modContainer = modContainer;
//        CustomGunNeoforge.sideExecutor = NeoSideExecutor.INSTANCE;
//        CustomGunNeoforge.registrarFactory = NeoRegistrarFactory.INSTANCE;
//        CustomGunNeoforge.mcRegistry = NeoRegistry.INSTANCE;
//        CustomGunNeoforge.capabilityProvider = NeoCapabilityProvider.INSTANCE;
        CustomGunNeoforge.networkAdapter = new NeoNetworkAdapter(modEventBus, CustomGunNeoforge.mcRegistry, CustomGun.MOD_ID, NetworkHandler.protocol_version);
//        CustomGunNeoforge.networkHook = NeoNetworkHook.INSTANCE;
//        CustomGunNeoforge.eventRegister = NeoEventRegister.INSTANCE;
//        CustomGunNeoforge.modConfigSpecBuilderSupplier = NeoModConfigSpecBuilder::new;
//        CustomGunNeoforge.accessTransformer = NeoAccessTransformer.INSTANCE;
        Dist dist = CustomGunNeoforge.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CustomGun.init(mcSide, CustomGunNeoforge.sideExecutor,
                CustomGunNeoforge.registrarFactory, CustomGunNeoforge.mcRegistry, CustomGunNeoforge.capabilityProvider,
                CustomGunNeoforge.networkAdapter, CustomGunNeoforge.networkHook,
                CustomGunNeoforge.eventRegister,
                CustomGunNeoforge.modConfigSpecBuilderSupplier,
                CustomGunNeoforge.accessTransformer);

        NeoCapabilityRegistry.onRegisterCapabilities(modEventBus);

        ModItems.ITEMS.registerAll(modEventBus);
        ModEntities.ENTITY_TYPES.registerAll(modEventBus);
        ModCreativeTabs.TABS.registerAll(modEventBus);
        ModRecipe.RECIPE_SERIALIZERS.registerAll(modEventBus);
        ModRecipe.RECIPE_SERIALIZERS_OLD1.registerAll(modEventBus);
        ModRecipe.RECIPE_TYPES.registerAll(modEventBus);
        ModRecipe.RECIPE_TYPES_OLD1.registerAll(modEventBus);
        ModSounds.SOUNDS.registerAll(modEventBus);
        ModParticles.PARTICLE_TYPES.registerAll(modEventBus);

        if (mcSide == McSide.CLIENT) {
            _CustomGunNeoforgeClient.init();
        }
    }

    public static Dist getDist() {
        // [1.20.2, 1.21.10)
//        return FMLLoader.getDist();

        // [1.21.10neoforge, )
        return FMLLoader.getCurrent().getDist();
    }

    private static class _CustomGunNeoforgeClient {
        public static void init() {
            CustomGunNeoforgeClient.init();
        }
    }

    @ApiStatus.AvailableSince("neoforge1.21.1")
    public static ModContainer modContainer;
}
