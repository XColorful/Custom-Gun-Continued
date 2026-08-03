/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.forge.CustomGunForge;

public class ForgeRegistry implements IMcRegistry {

    @Override public @Nullable ResourceLocation createResourceLocation(String rlString) {
        return ResourceLocation.tryParse(rlString);
    }
    @Override public <T> ResourceKey<T> createResourceKey(ResourceKey<? extends Registry<T>> registryName, ResourceLocation rl) {
        return ResourceKey.create(registryName, rl);
    }

    @Override public @Nullable Block getBlock(ResourceLocation rl) {
        return ForgeRegistries.BLOCKS.getValue(rl);
    }
    @Override public @Nullable ResourceLocation getBlockRl(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }
    @Override public @Nullable ParticleType<?> getParticleType(ResourceLocation rl) {
        return ForgeRegistries.PARTICLE_TYPES.getValue(rl);
    }
    @Override public @Nullable ResourceLocation getParticleTypeRl(ParticleType<?> particleType) {
        return ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
    }
    @Override public @Nullable MobEffect getMobEffect(ResourceLocation rl) {
        return ForgeRegistries.MOB_EFFECTS.getValue(rl);
    }
    @Override public @Nullable MobEffect getMobEffect_orHolder(ResourceLocation rl) {
        return ForgeRegistries.MOB_EFFECTS.getValue(rl);
    }
    @Override public @Nullable ResourceLocation getMobEffectRl(MobEffect mobEffect) {
        return ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
    }
    @Override public @Nullable Item getItem(ResourceLocation rl) {
        return ForgeRegistries.ITEMS.getValue(rl);
    }
    @Override public @Nullable ResourceLocation getItemRl(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }
    @Override public @Nullable EntityType<?> getEntityType(ResourceLocation rl) {
        return ForgeRegistries.ENTITY_TYPES.getValue(rl); // 无论输入不存在的RL还是null都不会返回null，小Forge露出猪脚了吧[doge]
    }
    @Override public @Nullable ResourceLocation getEntityTypeRl(EntityType<?> entityType) {
        return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public MinecraftServer getMinecraftServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public @Nullable RegistryAccess getRegistryAccess() {
        MinecraftServer server = getMinecraftServer();
        if (server != null) {
            return server.registryAccess();
        } else if (CustomGunForge.sideExecutor.getLogicalSide().isClient()) {
            return _ClientAccess.get();
        } else {
            return null;
        }
    }

    private static class _ClientAccess {
        private static RegistryAccess get() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                return mc.level.registryAccess();
            } else if (mc.player != null) {
                return mc.player.registryAccess();
            }
            return null;
        }
    }
}
