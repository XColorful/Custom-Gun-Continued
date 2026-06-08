/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.init.registry;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

/**
 * 平台无关的注册对象引用接口，用于替代 Forge 的 RegistryObject。
 */
public interface IRegistryObject<T> extends Supplier<T> {

    /**
     * @return 注册的名称，如 "loot_spawner"
     */
    String getId();

    /**
     * @return 实际的注册对象
     */
    @Override
    T get();

    /**
     * @return 完整的注册名称，"namespace:location"
     */
    Identifier getRegistryName();
}