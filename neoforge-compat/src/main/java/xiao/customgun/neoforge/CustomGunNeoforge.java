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

package xiao.customgun.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.core.api.init.registry.IRegistrarFactory;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.INetworkHook;
import xiao.customgun.core.init.registry.ModRecipe;
import xiao.customgun.core.init.registry.ModSounds;
import xiao.customgun.neoforge.common.NeoSideExecutor;
import xiao.customgun.neoforge.config.NeoModConfigSpecBuilder;
import xiao.customgun.neoforge.event.NeoEventRegister;
import xiao.customgun.neoforge.init.registry.NeoRegistrarFactory;
import xiao.customgun.neoforge.minecraft.NeoRegistry;
import xiao.customgun.neoforge.network.NeoNetworkAdapter;
import xiao.customgun.neoforge.network.NeoNetworkHook;
import xiao.customgun.neoforgeclient.CustomGunNeoforgeClient;

import java.util.function.Supplier;

@Mod(CustomGun.MOD_ID)
public class CustomGunNeoforge {

    public static ISideExecutor sideExecutor;
    public static IRegistrarFactory registrarFactory;
    public static IMcRegistry mcRegistry;
    public static INetworkAdapter networkAdapter;
    public static INetworkHook networkHook;
    public static IEventRegister eventRegister;
    public static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier;

    public CustomGunNeoforge(IEventBus modEventBus) {
        CustomGunNeoforge.sideExecutor = new NeoSideExecutor();
        CustomGunNeoforge.registrarFactory = new NeoRegistrarFactory();
        CustomGunNeoforge.mcRegistry = new NeoRegistry();
        CustomGunNeoforge.networkAdapter = new NeoNetworkAdapter();
        CustomGunNeoforge.networkHook = new NeoNetworkHook();
        CustomGunNeoforge.eventRegister = new NeoEventRegister();
        CustomGunNeoforge.modConfigSpecBuilderSupplier = NeoModConfigSpecBuilder::new;
        Dist dist = FMLLoader.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CustomGun.init(mcSide, CustomGunNeoforge.sideExecutor,
                CustomGunNeoforge.registrarFactory, CustomGunNeoforge.mcRegistry,
                CustomGunNeoforge.networkAdapter, CustomGunNeoforge.networkHook,
                CustomGunNeoforge.eventRegister,
                CustomGunNeoforge.modConfigSpecBuilderSupplier);

        ModRecipe.RECIPE_SERIALIZERS.registerAll(modEventBus);
        ModRecipe.RECIPE_TYPES.registerAll(modEventBus);
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
