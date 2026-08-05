/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.init.registry;

import dev.xcolorful.customgun.core.api.init.registry.IMenuTypeFactory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;

/**
 * IMenuTypeFactory 的 NeoForge 实现，使用 MenuType 的构造函数来适配网络菜单创建。
 */
public class NeoMenuTypeFactory implements IMenuTypeFactory {

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createBlockEntityMenu(IMenuTypeFactory.IMenuTypeCreationHandler<T> factory) {
        IContainerFactory<T> neoForgeFactory = factory::create;
        return IMenuTypeExtension.create(neoForgeFactory);
    }
}