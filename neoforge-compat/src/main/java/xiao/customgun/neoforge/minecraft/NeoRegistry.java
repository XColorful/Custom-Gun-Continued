/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.neoforge.CustomGunNeoforge;

public class NeoRegistry implements IMcRegistry {

    @Override public @Nullable Identifier createResourceLocation(String rlString) {
        return Identifier.tryParse(rlString);
    }
    @Override public <T> ResourceKey<T> createResourceKey(ResourceKey<? extends Registry<T>> registryName, Identifier rl) {
        return ResourceKey.create(registryName, rl);
    }

    @Override public @Nullable Block getBlock(Identifier rl) {
        return BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
    }
    @Override public @Nullable Identifier getBlockRl(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }
    @Override public @Nullable ParticleType<?> getParticleType(Identifier rl) {
        return BuiltInRegistries.PARTICLE_TYPE.getOptional(rl).orElse(null);
    }
    @Override public @Nullable Identifier getParticleTypeRl(ParticleType<?> particleType) {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(particleType);
    }
    @Override public @Nullable MobEffect getMobEffect(Identifier rl) {
        return BuiltInRegistries.MOB_EFFECT.getOptional(rl).orElse(null);
    }
    @Override public @Nullable Holder<MobEffect> getMobEffect_orHolder(Identifier rl) {
        var reference = BuiltInRegistries.MOB_EFFECT.get(rl).orElse(null); return reference != null ? reference.getDelegate() : null;
    }
    @Override public @Nullable Identifier getMobEffectRl(MobEffect mobEffect) {
        return BuiltInRegistries.MOB_EFFECT.getKey(mobEffect);
    }
    @Override public @Nullable Item getItem(Identifier rl) {
        return BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
    }
    @Override public @Nullable Identifier getItemRl(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
    @Override public @Nullable EntityType<?> getEntityType(Identifier rl) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
    }
    @Override public @Nullable Identifier getEntityTypeRl(EntityType<?> entityType) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
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
        } else if (CustomGunNeoforge.sideExecutor.getLogicalSide().isClient()) {
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