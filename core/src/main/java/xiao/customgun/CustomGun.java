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
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.event.ICustomEventPoster;
import xiao.customgun.core.api.event.ICustomEventRegister;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.core.api.gun.IGunManager;
import xiao.customgun.core.api.init.registry.IRegistrarFactory;
import xiao.customgun.core.api.minecraft.ICapabilityProvider;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.INetworkHook;
import xiao.customgun.core.api.projectile.IProjectileManager;
import xiao.customgun.core.event.EventPoster;
import xiao.customgun.core.event.EventRegister;
import xiao.customgun.core.event.custom.CoreEventHandlers;
import xiao.customgun.core.gun.GunManager;
import xiao.customgun.core.init.ModConfig;
import xiao.customgun.core.network.NetworkHandler;
import xiao.customgun.core.network.NetworkHook;
import xiao.customgun.core.projectile.ProjectileManager;

import java.util.function.Supplier;

public class CustomGun {
    public static final String MOD_ID = "customgun";
    public static final String MOD_ID_SHORT = "cgc";
    public static final String MOD_ID_OLD1 = "tacz";
    public static final Logger LOGGER = LogUtils.getLogger();

    protected static boolean initialized;
    protected static McSide mcSide = McSide.CLIENT;
    protected static ISideExecutor sideExecutor;
    private static IRegistrarFactory registrarFactory;
    private static IMcRegistry mcRegistry;
    private static ICapabilityProvider capabilityProvider;
    private static Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier;

    public static void init(McSide mcSide, ISideExecutor sideExecutor,
                            IRegistrarFactory factory, IMcRegistry mcRegistry, ICapabilityProvider capabilityProvider,
                            INetworkAdapter networkAdapter, INetworkHook networkHook,
                            IEventRegister eventRegister,
                            Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier) {
        if (initialized) return;
        CustomGun.mcSide = mcSide;
        CustomGun.sideExecutor = sideExecutor;
        CustomGun.registrarFactory = factory;
        CustomGun.mcRegistry = mcRegistry;
        CustomGun.capabilityProvider = capabilityProvider;
        CustomGun.modConfigSpecBuilderSupplier = modConfigSpecBuilderSupplier;

        // 最早的事件机制初始化
        EventRegister.initialize(eventRegister);

        NetworkHandler.initialize(networkAdapter);
        NetworkHook.initialize(networkHook);

        ModConfig.init();

        gunManager = GunManager.INSTANCE;
        projectileManager = ProjectileManager.INSTANCE;

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
    public static ICapabilityProvider getCapabilityProvider() {
        if (capabilityProvider == null) {
            throw new IllegalStateException("Capability provider has not been initialized. Call init() first.");
        }
        return capabilityProvider;
    }
    public static IModConfigSpecBuilder getModConfigSpecBuilder() {
        return modConfigSpecBuilderSupplier.get();
    }
    public static MinecraftServer getMinecraftServer() {
        return getMcRegistry().getMinecraftServer();
    }

    private final @NotNull static ICustomEventRegister eventRegister = EventRegister.get(); // 在模组加载前就能触发
    private final @NotNull static ICustomEventPoster eventPoster = EventPoster.get(); // 在模组加载前就能触发
    private static IGunManager gunManager;
    private static IProjectileManager projectileManager;
    public @NotNull static ICustomEventRegister getEventRegister() {
        return CustomGun.eventRegister;
    }
    public @NotNull static ICustomEventPoster getEventPoster() {
        return CustomGun.eventPoster;
    }
    public static IGunManager getGunManager() {
        return CustomGun.gunManager;
    }
    public static IProjectileManager getProjectileManager() {
        return CustomGun.projectileManager;
    }
    /**
     * @deprecated 除非需要深度定制, 否则不应该调用
     */
    @Deprecated(forRemoval = false)
    public static void setGunManager(@NotNull IGunManager gunManager) {
        CustomGun.gunManager = gunManager;
    }
    @Deprecated(forRemoval = false)
    public static void setProjectileManager(@NotNull IProjectileManager projectileManager) {
        CustomGun.projectileManager = projectileManager;
    }
}
