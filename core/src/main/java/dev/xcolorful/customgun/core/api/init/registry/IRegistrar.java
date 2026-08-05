/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.init.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 平台无关的注册对象集合接口。
 * 核心模块通过此接口定义内容，兼容层负责实际的注册实现。
 */
public interface IRegistrar<T> {

    /**
     * 注册一个新的对象到集合中。
     * @param name 对象的注册名
     * @param supplier 对象的构造函数
     * @return 平台无关的注册对象引用
     */
    <V extends T> IRegistryObject<V> register(final String name, final Supplier<? extends V> supplier);

    /**
     * 平台兼容层必须实现此方法以执行实际的注册操作（如注册到 Forge 的 IEventBus）。
     * (在 Forge 兼容层中，此方法会调用 DeferredRegister.register(bus))
     * @param registrarHook 实际的注册钩子，例如 Forge 的 IEventBus
     */
    void registerAll(Object registrarHook);

    // --------hack--------

    /**
     * 解决以下问题:
     * <ul>
     *     <li>{@link dev.xcolorful.customgun.core}不能包含forge/neoforge import
     *     <li>mixin无法通过注入函数来实现重载
     *     <li>{@link dev.xcolorful.customgun.core}需要重载forge/neoforge接口
     * </ul>
     * (黑魔法)替换注册的类型, core仍然可以强转成core的类用
     * @return 被替换过的class supplier
     */
    <I extends Item> IRegistryObject<I> registerItem(final String name, final Class<I> clazz);
    /**
     * 解决以下问题:
     * <ul>
     *     <li>{@link dev.xcolorful.customgun.core}不能包含forge/neoforge import
     *     <li>mixin无法通过注入函数来实现重载
     *     <li>{@link dev.xcolorful.customgun.core}需要实现forge/neoforge接口
     * </ul>
     * (黑魔法)替换注册的类型, core仍然可以强转成core的类用
     * @return 被替换过的EntityType builder
     */
    <E extends Entity> IRegistryObject<EntityType<E>> registerEntity(final String name, final Class<E> clazz, final Function<EntityType.EntityFactory<E>, EntityType.Builder<E>> builderFactory);
}
