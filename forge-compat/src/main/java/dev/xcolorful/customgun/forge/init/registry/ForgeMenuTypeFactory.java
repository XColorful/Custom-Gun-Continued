/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forge.init.registry;

import dev.xcolorful.customgun.core.api.init.registry.IMenuTypeFactory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;

/**
 * IMenuTypeFactory 的 Forge 实现，使用 IForgeMenuType.create() 来实现网络传输的菜单创建。
 */
public class ForgeMenuTypeFactory implements IMenuTypeFactory {

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createBlockEntityMenu(IMenuTypeCreationHandler<T> factory) {
        return IForgeMenuType.create(factory::create);
    }
}