/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.init.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;

import java.util.function.Function;
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
    public <I extends Item> IRegistryObject<I> registerItem(String name, Class<I> clazz) {
        Supplier<? extends I> forgeItemSupplier = ForgeModItems.getForgeSupplier(clazz);
        Supplier<? extends T> finalSupplier = (Supplier<? extends T>) forgeItemSupplier;
        return (IRegistryObject<I>) this.register(name, finalSupplier);
    }
    @SuppressWarnings("unchecked")
    @Override
    public <E extends Entity> IRegistryObject<EntityType<E>> registerEntity(String name, Class<E> clazz, Function<EntityType.EntityFactory<E>, EntityType.Builder<E>> builderFactory) {
        EntityType.EntityFactory<E> factory = ForgeModEntities.getForgeFactory(clazz);
        RegistryObject<EntityType<E>> registryObject = ((DeferredRegister<EntityType<?>>) deferredRegister).register(name, () -> builderFactory.apply(factory).build(name));
        return new ForgeRegistryObject<>(registryObject, name);
    }
}
