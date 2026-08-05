/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

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

    public static ISideExecutor sideExecutor;
    public static IRegistrarFactory registrarFactory;
    public static IMcRegistry mcRegistry;
    public static ICapabilityProvider capabilityProvider;
    public static INetworkAdapter networkAdapter;
    public static INetworkHook networkHook;
    public static IEventRegister eventRegister;
    public static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier;
    public static ForgeAccessTransformer accessTransformer;

    public CustomGunForge() {
        CustomGunForge.sideExecutor = new ForgeSideExecutor();
        CustomGunForge.registrarFactory = new ForgeRegistrarFactory();
        CustomGunForge.mcRegistry = new ForgeRegistry();
        CustomGunForge.capabilityProvider = new ForgeCapabilityProvider();
        CustomGunForge.networkAdapter = new ForgeNetworkAdapter();
        CustomGunForge.networkHook = new ForgeNetworkHook();
        CustomGunForge.eventRegister = new ForgeEventRegister();
        CustomGunForge.modConfigSpecBuilderSupplier = ForgeModConfigSpecBuilder::new;
        CustomGunForge.accessTransformer = new ForgeAccessTransformer();
        Dist dist = FMLLoader.getDist();
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

        if (mcSide == McSide.CLIENT) {
            _GunModForgeClient.init();
        }
    }

    private static class _GunModForgeClient {
        public static void init() {
            CustomGunForgeClient.init();
        }
    }
}