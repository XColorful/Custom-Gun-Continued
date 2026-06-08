/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.init.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;

import java.util.function.Supplier;

/**
 * IRegistrar 的 NeoForge 实现，包装了 NeoForge 的 DeferredRegister。
 */
public class NeoRegistrar<T> implements IRegistrar<T> {
    private final DeferredRegister<T> deferredRegister;

    public NeoRegistrar(DeferredRegister<T> deferredRegister) {
        this.deferredRegister = deferredRegister;
    }

    @Override
    public <V extends T> IRegistryObject<V> register(String name, Supplier<? extends V> supplier) {
        DeferredHolder<T, V> deferredHolder = deferredRegister.register(name, supplier);
        CustomGun.LOGGER.debug("Registering NeoForge object: {}", name);
        return new NeoRegistryObject<>(deferredHolder, name);
    }

    @Override
    public void registerAll(Object registrarHook) {
        if (registrarHook instanceof IEventBus eventBus) {
            deferredRegister.register(eventBus);
        } else {
            CustomGun.LOGGER.error("Invalid registrar hook provided for NeoRegistrar: {}", registrarHook.getClass().getName());
        }
    }

    // --------hack--------

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> IRegistryObject<V> registerItem(String name, Class<? extends V> clazz) {
        Class<? extends Item> itemClass = (Class<? extends Item>) clazz;
        Supplier<? extends Item> forgeItemSupplier = NeoModItems.getNeoSupplier(itemClass);
        Supplier<? extends V> finalSupplier = (Supplier<? extends V>) forgeItemSupplier;
        return this.register(name, finalSupplier);
    }
}