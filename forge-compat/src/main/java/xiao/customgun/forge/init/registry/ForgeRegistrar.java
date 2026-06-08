/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.init.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;

import java.util.function.Supplier;

/**
 * IRegistrar 的 Forge 实现，包装了 Forge 的 DeferredRegister。
 */
public class ForgeRegistrar<T> implements IRegistrar<T> {
    private final DeferredRegister<T> deferredRegister;

    public ForgeRegistrar(DeferredRegister<T> deferredRegister) {
        this.deferredRegister = deferredRegister;
    }

    @Override
    public <V extends T> IRegistryObject<V> register(String name, Supplier<? extends V> supplier) {
        RegistryObject<V> registryObject = deferredRegister.register(name, supplier);
        CustomGun.LOGGER.debug("Registering Forge object: {}", name);
        return new ForgeRegistryObject<>(registryObject, name);
    }

    @Override
    public void registerAll(Object registrarHook) {
        if (registrarHook instanceof IEventBus eventBus) {
            deferredRegister.register(eventBus);
        } else {
            CustomGun.LOGGER.error("Invalid registrar hook provided for ForgeRegistrar: {}", registrarHook.getClass().getName());
        }
    }

    // --------hack--------

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> IRegistryObject<V> registerItem(String name, Class<? extends V> clazz) {
        Class<? extends Item> itemClass = (Class<? extends Item>) clazz;
        Supplier<? extends Item> forgeItemSupplier = ForgeModItems.getForgeSupplier(itemClass);
        Supplier<? extends V> finalSupplier = (Supplier<? extends V>) forgeItemSupplier;
        return this.register(name, finalSupplier);
    }
}
