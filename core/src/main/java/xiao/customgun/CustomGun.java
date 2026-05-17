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

package xiao.customgun;

import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.event.ICustomEventPoster;
import xiao.customgun.core.api.event.ICustomEventRegister;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.core.api.init.registry.IRegistrarFactory;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.INetworkHook;
import xiao.customgun.core.event.EventPoster;
import xiao.customgun.core.event.EventRegister;
import xiao.customgun.core.event.custom.CoreEventHandlers;
import xiao.customgun.core.init.ModConfig;
import xiao.customgun.core.network.NetworkHandler;
import xiao.customgun.core.network.NetworkHook;

import java.util.function.Supplier;

public class CustomGun {
    public static final String MOD_ID = "customgun";
    public static final String MOD_ID_SHORT = "cgc";
    public static final Logger LOGGER = LogUtils.getLogger();

    protected static boolean initialized;
    protected static McSide mcSide = McSide.CLIENT;
    protected static ISideExecutor sideExecutor;
    private static IRegistrarFactory registrarFactory;
    private static IMcRegistry mcRegistry;
    private static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier;

    public static void init(McSide mcSide, ISideExecutor sideExecutor,
                            IRegistrarFactory factory, IMcRegistry mcRegistry,
                            INetworkAdapter networkAdapter, INetworkHook networkHook,
                            IEventRegister eventRegister,
                            Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier) {
        if (initialized) return;
        CustomGun.mcSide = mcSide;
        CustomGun.sideExecutor = sideExecutor;
        CustomGun.registrarFactory = factory;
        CustomGun.mcRegistry = mcRegistry;
        CustomGun.modConfigSpecBuilderSupplier = modConfigSpecBuilderSupplier;

        // 最早的事件机制初始化
        EventRegister.initialize(eventRegister);

        NetworkHandler.initialize(networkAdapter);
        NetworkHook.initialize(networkHook);

        ModConfig.init();

        CoreEventHandlers.registerAll(getEventRegister());
        initialized = true;
    }

    public static McSide getMcSide() {
        return mcSide;
    }
    public static ISideExecutor getSideExecutor() {
        return sideExecutor;
    }
    public static IRegistrarFactory getRegistrarFactory() {
        if (registrarFactory == null) {
            throw new IllegalStateException("Registrar factory has not been initialized. Call init() first.");
        }
        return registrarFactory;
    }
    public static IMcRegistry getMcRegistry() {
        if (mcRegistry == null) {
            throw new IllegalStateException("Mc registry has not been initialized. Call init() first.");
        }
        return mcRegistry;
    }
    public static IModConfigSpecBuilder getModConfigSpecBuilder() {
        return modConfigSpecBuilderSupplier.get();
    }
    public static MinecraftServer getMinecraftServer() {
        return getMcRegistry().getMinecraftServer();
    }

    private final @NotNull static ICustomEventRegister eventRegister = EventRegister.get(); // 在模组加载前就能触发
    private final @NotNull static ICustomEventPoster eventPoster = EventPoster.get(); // 在模组加载前就能触发
    public @NotNull static ICustomEventRegister getEventRegister() {
        return CustomGun.eventRegister;
    }
    public @NotNull static ICustomEventPoster getEventPoster() {
        return CustomGun.eventPoster;
    }
    @ApiStatus.AvailableSince("1.21.1")
    public static @Nullable RegistryAccess getRegistryAccess() {
        RegistryAccess registryAccess = getMcRegistry().getRegistryAccess();
        if (registryAccess != null) {
            return registryAccess;
        } else {
            CustomGun.LOGGER.warn("Failed to retrieve registry access, return RegistryAccess.EMPTY");
            return RegistryAccess.EMPTY;
        }
    }
}
