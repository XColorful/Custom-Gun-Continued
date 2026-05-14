/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.init.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiao.customgun.core.api.init.registry.IMenuTypeFactory;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistrarFactory;

/**
 * IRegistrarFactory 的 NeoForge 实现，负责创建基于 NeoForge API 的注册器。
 */
public class NeoRegistrarFactory implements IRegistrarFactory {

    private final IMenuTypeFactory menuTypeFactory = new NeoMenuTypeFactory();

    // 通用方法：与 Forge 实现完全一样
    @Override
    public <T> IRegistrar<T> create(String modId, Registry<T> registry) {
        return new NeoRegistrar<>(DeferredRegister.create(registry.key(), modId));
    }

    // 特定类型方法：将 ForgeRegistries.XXX 替换为 Registries.XXX
    @Override
    public IRegistrar<Block> createBlocks(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.BLOCK, modId));
    }

    @Override
    public IRegistrar<BlockEntityType<?>> createBlockEntities(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId));
    }

    @Override
    public IRegistrar<Item> createItems(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.ITEM, modId));
    }

    @Override
    public IRegistrar<CreativeModeTab> createCreativeTabs(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId));
    }

    @Override
    public IRegistrar<MenuType<?>> createMenuTypes(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.MENU, modId));
    }

    @Override
    public IRegistrar<SoundEvent> createSounds(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.SOUND_EVENT, modId));
    }

    @Override
    public IRegistrar<EntityType<?>> createEntityTypes(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.ENTITY_TYPE, modId));
    }

    @Override
    public IRegistrar<RecipeSerializer<?>> createRecipeSerializers(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.RECIPE_SERIALIZER, modId));
    }

    @Override
    public IRegistrar<RecipeType<?>> createRecipes(String modId) {
        return new NeoRegistrar<>(DeferredRegister.create(Registries.RECIPE_TYPE, modId));
    }

    @Override
    public IMenuTypeFactory getMenuTypeFactory() {
        return menuTypeFactory;
    }
}