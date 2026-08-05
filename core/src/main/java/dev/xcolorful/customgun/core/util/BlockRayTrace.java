/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.core.init.registry.ModBlocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Go to {@link RayTraceUtils}
 */
@Deprecated(forRemoval = true)
public class BlockRayTrace {

    @Deprecated
    public static BlockHitResult rayTraceBlocks(Level level, ClipContext context) {
        return RayTraceUtils.BlockTrace.rayTraceBlocksWithFilter(
                level,
                context.getFrom(),
                context.getTo(),
                (blockState, blockPos) -> context.getBlockShape(blockState, level, blockPos),
                (fluidState, blockPos) -> context.getFluidShape(fluidState, level, blockPos),
                blockState -> {
                    if (blockState == null) {
                        return false;
                    }
                    return blockState.is(ModBlocks.BULLET_IGNORE_BLOCKS);
                }
        );
    }
}
