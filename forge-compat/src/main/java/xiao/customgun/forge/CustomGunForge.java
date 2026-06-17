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

package xiao.customgun.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.core.api.init.registry.IRegistrarFactory;
import xiao.customgun.core.api.minecraft.ICapabilityProvider;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.INetworkHook;
import xiao.customgun.core.init.registry.*;
import xiao.customgun.forge.common.ForgeSideExecutor;
import xiao.customgun.forge.config.ForgeModConfigSpecBuilder;
import xiao.customgun.forge.event.ForgeEventRegister;
import xiao.customgun.forge.init.registry.ForgeRegistrarFactory;
import xiao.customgun.forge.minecraft.ForgeCapabilityProvider;
import xiao.customgun.forge.minecraft.ForgeRegistry;
import xiao.customgun.forge.network.ForgeNetworkAdapter;
import xiao.customgun.forge.network.ForgeNetworkHook;
import xiao.customgun.forgeclient.CustomGunForgeClient;

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

    public CustomGunForge() {
        CustomGunForge.sideExecutor = new ForgeSideExecutor();
        CustomGunForge.registrarFactory = new ForgeRegistrarFactory();
        CustomGunForge.mcRegistry = new ForgeRegistry();
        CustomGunForge.capabilityProvider = new ForgeCapabilityProvider();
        CustomGunForge.networkAdapter = new ForgeNetworkAdapter();
        CustomGunForge.networkHook = new ForgeNetworkHook();
        CustomGunForge.eventRegister = new ForgeEventRegister();
        CustomGunForge.modConfigSpecBuilderSupplier = ForgeModConfigSpecBuilder::new;
        Dist dist = FMLLoader.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CustomGun.init(mcSide, CustomGunForge.sideExecutor,
                CustomGunForge.registrarFactory, CustomGunForge.mcRegistry, CustomGunForge.capabilityProvider,
                CustomGunForge.networkAdapter, CustomGunForge.networkHook,
                CustomGunForge.eventRegister,
                CustomGunForge.modConfigSpecBuilderSupplier);

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