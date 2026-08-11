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

package dev.xcolorful.customgun;

import com.mojang.logging.LogUtils;
import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.event.ICustomEventPoster;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.core.api.gun.IGunManager;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrarFactory;
import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.minecraft.access.ICoreAccessTransformer;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.INetworkHook;
import dev.xcolorful.customgun.core.api.projectile.IProjectileManager;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderManager;
import dev.xcolorful.customgun.core.event.EventPoster;
import dev.xcolorful.customgun.core.event.EventRegister;
import dev.xcolorful.customgun.core.event.custom.CoreEventHandlers;
import dev.xcolorful.customgun.core.gun.GunManager;
import dev.xcolorful.customgun.core.init.ModConfig;
import dev.xcolorful.customgun.core.network.NetworkHandler;
import dev.xcolorful.customgun.core.network.NetworkHook;
import dev.xcolorful.customgun.core.projectile.ProjectileManager;
import dev.xcolorful.customgun.core.text.placeholder.PlaceholderManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

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
    private static ICoreAccessTransformer accessTransformer;

    public static void init(McSide mcSide, ISideExecutor sideExecutor,
                            IRegistrarFactory factory, IMcRegistry mcRegistry, ICapabilityProvider capabilityProvider,
                            INetworkAdapter networkAdapter, INetworkHook networkHook,
                            IEventRegister eventRegister,
                            Supplier<IModConfigSpecBuilder> modConfigSpecBuilderSupplier,
                            ICoreAccessTransformer accessTransformer) {
        if (initialized) return;
        CustomGun.mcSide = mcSide;
        CustomGun.sideExecutor = sideExecutor;
        CustomGun.registrarFactory = factory;
        CustomGun.mcRegistry = mcRegistry;
        CustomGun.capabilityProvider = capabilityProvider;
        CustomGun.modConfigSpecBuilderSupplier = modConfigSpecBuilderSupplier;
        CustomGun.accessTransformer = accessTransformer;

        // 最早的事件机制初始化
        EventRegister.initialize(eventRegister);

        NetworkHandler.initialize(networkAdapter);
        NetworkHook.initialize(networkHook);

        ModConfig.init();

        gunManager = GunManager.INSTANCE;
        GunManager.init(mcSide);
        projectileManager = ProjectileManager.INSTANCE;
        ProjectileManager.init(mcSide);
        placeholderManager = PlaceholderManager.INSTANCE;
        PlaceholderManager.init(mcSide);

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
    public static ICoreAccessTransformer getAccessTransformer() {
        return accessTransformer;
    }

    private final @NotNull static ICustomEventRegister eventRegister = EventRegister.get(); // 在模组加载前就能触发
    private final @NotNull static ICustomEventPoster eventPoster = EventPoster.get(); // 在模组加载前就能触发
    private static IGunManager gunManager;
    private static IProjectileManager projectileManager;
    private static IPlaceholderManager placeholderManager;
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
    public static IPlaceholderManager getPlaceholderManager() {
        return CustomGun.placeholderManager;
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
    @Deprecated(forRemoval = false)
    public static void setPlaceholderManager(@NotNull IPlaceholderManager placeholderManager) {
        CustomGun.placeholderManager = placeholderManager;
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
