/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.minecraft;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IMcRegistry {

    @Nullable Identifier createResourceLocation(String rlString);
    <T> ResourceKey<T> createResourceKey(ResourceKey<? extends Registry<T>> registryName, Identifier rl);

    @Nullable Block getBlock(Identifier rl);
    @Nullable Identifier getBlockRl(Block block);
    @Nullable ParticleType<?> getParticleType(Identifier rl);
    @Nullable Identifier getParticleTypeRl(ParticleType<?> particleType);
    @Nullable MobEffect getMobEffect(Identifier rl);
    /**
     * @since 1.21.1 返回{@code Holder<MobEffect>}
     */
    @Nullable Holder<MobEffect> getMobEffect_orHolder(Identifier rl);
    @Nullable Identifier getMobEffectRl(MobEffect mobEffect);
    @Nullable Item getItem(Identifier rl);
    @Nullable Identifier getItemRl(Item item);
    @Nullable EntityType<?> getEntityType(Identifier rl);
    @Nullable Identifier getEntityTypeRl(EntityType<?> entityType);

    boolean isModLoaded(String modId);

    MinecraftServer getMinecraftServer();

    @ApiStatus.AvailableSince("1.21.1")
    @Nullable RegistryAccess getRegistryAccess();
}
