package dev.xcolorful.customgun.core.init.registry;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlocks {

    public static TagKey<Block> BULLET_IGNORE_BLOCKS = BlockTags.create(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "bullet_ignore")));
}
