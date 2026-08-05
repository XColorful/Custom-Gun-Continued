/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrar;
import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
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
    public <I extends Item> IRegistryObject<I> registerItem(String name, Class<I> clazz) {
        Supplier<? extends I> forgeItemSupplier = NeoModItems.getNeoSupplier(clazz);
        Supplier<? extends T> finalSupplier = (Supplier<? extends T>) forgeItemSupplier;
        return (IRegistryObject<I>) this.register(name, finalSupplier);
    }
    @SuppressWarnings("unchecked")
    @Override
    public <E extends Entity> IRegistryObject<EntityType<E>> registerEntity(String name, Class<E> clazz, Function<EntityType.EntityFactory<E>, EntityType.Builder<E>> builderFactory) {
        EntityType.EntityFactory<E> factory = NeoModEntities.getNeoFactory(clazz);
        ResourceKey<EntityType<?>> nameRk = ResourceKey.create(Registries.ENTITY_TYPE, CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:%s", deferredRegister.getNamespace(), name)));
        DeferredHolder<EntityType<?>, EntityType<E>> deferredHolder = ((DeferredRegister<EntityType<?>>) deferredRegister).register(name, () -> builderFactory.apply(factory).build(nameRk));
        return new NeoRegistryObject<>(deferredHolder, name);
    }
}