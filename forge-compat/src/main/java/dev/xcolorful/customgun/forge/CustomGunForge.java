package dev.xcolorful.customgun.forge;

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
import dev.xcolorful.customgun.forge.common.ForgeSideExecutor;
import dev.xcolorful.customgun.forge.config.ForgeModConfigSpecBuilder;
import dev.xcolorful.customgun.forge.event.ForgeEventRegister;
import dev.xcolorful.customgun.forge.init.registry.ForgeRegistrarFactory;
import dev.xcolorful.customgun.forge.minecraft.ForgeCapabilityProvider;
import dev.xcolorful.customgun.forge.minecraft.ForgeRegistry;
import dev.xcolorful.customgun.forge.minecraft.access.ForgeAccessTransformer;
import dev.xcolorful.customgun.forge.network.ForgeNetworkAdapter;
import dev.xcolorful.customgun.forge.network.ForgeNetworkHook;
import dev.xcolorful.customgun.forgeclient.CustomGunForgeClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.function.Supplier;

@Mod(CustomGun.MOD_ID)
public class CustomGunForge {

    public static ISideExecutor sideExecutor = ForgeSideExecutor.INSTANCE;
    public static IRegistrarFactory registrarFactory = ForgeRegistrarFactory.INSTANCE;
    public static IMcRegistry mcRegistry = ForgeRegistry.INSTANCE;
    public static ICapabilityProvider capabilityProvider = ForgeCapabilityProvider.INSTANCE;
    public static INetworkAdapter networkAdapter;
    public static INetworkHook networkHook = ForgeNetworkHook.INSTANCE;
    public static IEventRegister eventRegister = ForgeEventRegister.INSTANCE;
    public static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier = ForgeModConfigSpecBuilder::new;
    public static ForgeAccessTransformer accessTransformer = ForgeAccessTransformer.INSTANCE;

    public CustomGunForge() {
//        CustomGunForge.sideExecutor = ForgeSideExecutor.INSTANCE;
//        CustomGunForge.registrarFactory = ForgeRegistrarFactory.INSTANCE;
//        CustomGunForge.mcRegistry = ForgeRegistry.INSTANCE;
//        CustomGunForge.capabilityProvider = ForgeCapabilityProvider.INSTANCE;
        CustomGunForge.networkAdapter = new ForgeNetworkAdapter(CustomGunForge.mcRegistry, CustomGun.MOD_ID, NetworkHandler.protocol_version);
//        CustomGunForge.networkHook = ForgeNetworkHook.INSTANCE;
//        CustomGunForge.eventRegister = ForgeEventRegister.INSTANCE;
//        CustomGunForge.modConfigSpecBuilderSupplier = ForgeModConfigSpecBuilder::new;
//        CustomGunForge.accessTransformer = ForgeAccessTransformer.INSTANCE;
        Dist dist = CustomGunForge.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CustomGun.init(mcSide, CustomGunForge.sideExecutor,
                CustomGunForge.registrarFactory, CustomGunForge.mcRegistry, CustomGunForge.capabilityProvider,
                CustomGunForge.networkAdapter, CustomGunForge.networkHook,
                CustomGunForge.eventRegister,
                CustomGunForge.modConfigSpecBuilderSupplier,
                CustomGunForge.accessTransformer);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

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
            _CustomGunForgeClient.init();
        }
    }

    public static Dist getDist() {
        // [1.20.1, 1.21.10)
        return FMLLoader.getDist();

        // [1.21.10neoforge, )
//        return FMLLoader.getCurrent().getDist();
    }

    private static class _CustomGunForgeClient {
        public static void init() {
            CustomGunForgeClient.init();
        }
    }
}