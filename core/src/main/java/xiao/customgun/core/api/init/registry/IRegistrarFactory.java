/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.init.registry;

import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * 抽象注册器工厂：用于创建平台无关的注册对象集合 (IRegistrar)。
 * 具体的实现（如 ForgeRegistrarFactory）将在兼容层中完成。
 */
public interface IRegistrarFactory {

    // 通用的内容注册器接口
    <T> IRegistrar<T> create(String modId, Registry<T> registry);

    // 针对特定类型的便捷方法，简化代码
    IRegistrar<Block> createBlocks(String modId);
    IRegistrar<BlockEntityType<?>> createBlockEntities(String modId);
    IRegistrar<Item> createItems(String modId);
    IRegistrar<CreativeModeTab> createCreativeTabs(String modId);
    IRegistrar<MenuType<?>> createMenuTypes(String modId);
    IRegistrar<SoundEvent> createSounds(String modId);
    IRegistrar<EntityType<?>> createEntityTypes(String modId);
    IRegistrar<RecipeSerializer<?>> createRecipeSerializers(String modId);
    IRegistrar<RecipeType<?>> createRecipes(String modId);

    IMenuTypeFactory getMenuTypeFactory();
}
