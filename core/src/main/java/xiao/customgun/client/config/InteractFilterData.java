/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.config;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.resource.data.tag.InteractKeyType;
import xiao.customgun.core.config.SyncConfig;

import java.util.HashMap;
import java.util.Map;

public class InteractFilterData {
    private static boolean DEFAULT_RESULT = false;
    // 方块
    private static final Map<Identifier, Boolean> BLOCK_FILTER = new HashMap<>();
    // 实体
    private static final Map<Identifier, Boolean> ENTITY_FILTER = new HashMap<>();

    public static void reloadInteractFilter() {
        BLOCK_FILTER.clear();
        ENTITY_FILTER.clear();
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        // 方块
        for (String blockEntry : SyncConfig.INTERACT_KEY_BLACKLIST_BLOCKS.get())
            BLOCK_FILTER.put(mcRegistry.createResourceLocation(blockEntry), false);
        for (String blockEntry : SyncConfig.INTERACT_KEY_WHITELIST_BLOCKS.get())
            BLOCK_FILTER.put(mcRegistry.createResourceLocation(blockEntry), true);

        // 实体
        for (String entityEntry : SyncConfig.INTERACT_KEY_BLACKLIST_ENTITIES.get())
            ENTITY_FILTER.put(mcRegistry.createResourceLocation(entityEntry), false);
        for (String entityEntry : SyncConfig.INTERACT_KEY_WHITELIST_ENTITIES.get())
            ENTITY_FILTER.put(mcRegistry.createResourceLocation(entityEntry), true);
    }

    @ApiStatus.Internal
    public static boolean addBlockFilter(Identifier rl, @Nullable Boolean allowed) {
        if (allowed == null) {
            return BLOCK_FILTER.remove(rl) != null;
        } else {
            return BLOCK_FILTER.put(rl, allowed) != null;
        }
    }
    public static void setDefaultResult(boolean result) {
        DEFAULT_RESULT = result;
    }

    public static boolean canInteract(BlockState blockState) {
        Block block = blockState.getBlock();
        // ResourceLocation 过滤
        Boolean allowed = BLOCK_FILTER.get(CustomGun.getMcRegistry().getBlockRl(block));
        if (allowed != null) return allowed;
        // TagKey 过滤
        if (blockState.is(InteractKeyType.BLOCK.getBlacklist())) return false;
        if (blockState.is(InteractKeyType.BLOCK.getWhitelist())) return true;
        // 默认结果
        return DEFAULT_RESULT;
    }
    public static boolean canInteract(Entity entity) {
        EntityType<?> type = entity.getType();
        // ResourceLocation 过滤
        Boolean allowed = ENTITY_FILTER.get(CustomGun.getMcRegistry().getEntityTypeRl(type));
        if (allowed != null) return allowed;
        // TagKey 过滤
        if (type.is(InteractKeyType.ENTITY.getBlacklist())) return false;
        if (type.is(InteractKeyType.ENTITY.getWhitelist())) return true;
        // 默认结果
        return DEFAULT_RESULT;
    }
}
