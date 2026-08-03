/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import xiao.customgun.CustomGun;

public class ModBlocks {

    public static TagKey<Block> BULLET_IGNORE_BLOCKS = BlockTags.create(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "bullet_ignore")));
}
