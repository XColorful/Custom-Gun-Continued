/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.minecraft;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IMcRegistry {

    @Nullable ResourceLocation createResourceLocation(String rlString);
    <T> ResourceKey<T> createResourceKey(ResourceKey<? extends Registry<T>> registryName, ResourceLocation rl);

    @Nullable Block getBlock(ResourceLocation rl);
    @Nullable ResourceLocation getBlockRl(Block block);
    @Nullable ParticleType<?> getParticleType(ResourceLocation rl);
    @Nullable ResourceLocation getParticleTypeRl(ParticleType<?> particleType);
    @Nullable MobEffect getMobEffect(ResourceLocation rl);
    /**
     * @since 1.21.1 返回{@code Holder<MobEffect>}
     */
    @Nullable Holder<MobEffect> getMobEffect_orHolder(ResourceLocation rl);
    @Nullable ResourceLocation getMobEffectRl(MobEffect mobEffect);
    @Nullable Item getItem(ResourceLocation rl);
    @Nullable ResourceLocation getItemRl(Item item);
    @Nullable EntityType<?> getEntityType(ResourceLocation rl);
    @Nullable ResourceLocation getEntityTypeRl(EntityType<?> entityType);

    boolean isModLoaded(String modId);

    MinecraftServer getMinecraftServer();

    @ApiStatus.AvailableSince("1.21.1")
    @Nullable RegistryAccess getRegistryAccess();
}
