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
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

import java.util.function.Supplier;

@Mod(CustomGun.MOD_ID)
public class CustomGunNeoforge {

    public static ISideExecutor sideExecutor;
    public static IRegistrarFactory registrarFactory;
    public static IMcRegistry mcRegistry;
    public static ICapabilityProvider capabilityProvider;
    public static INetworkAdapter networkAdapter;
    public static INetworkHook networkHook;
    public static IEventRegister eventRegister;
    public static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier;
    public static NeoAccessTransformer accessTransformer;

    public CustomGunNeoforge(IEventBus modEventBus) {
        CustomGunNeoforge.sideExecutor = new NeoSideExecutor();
        CustomGunNeoforge.registrarFactory = new NeoRegistrarFactory();
        CustomGunNeoforge.mcRegistry = new NeoRegistry();
        CustomGunNeoforge.capabilityProvider = new NeoCapabilityProvider();
        CustomGunNeoforge.networkAdapter = NeoNetworkAdapter.INSTANCE;
        CustomGunNeoforge.networkHook = new NeoNetworkHook();
        CustomGunNeoforge.eventRegister = new NeoEventRegister();
        CustomGunNeoforge.modConfigSpecBuilderSupplier = NeoModConfigSpecBuilder::new;
        CustomGunNeoforge.accessTransformer = new NeoAccessTransformer();
        Dist dist = FMLLoader.getDist();
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

        if (mcSide == McSide.CLIENT) {
            _GunModNeoforgeClient.init();
        }
    }

    private static class _GunModNeoforgeClient {
        public static void init() {
            CustomGunNeoforgeClient.init();
        }
    }
}
