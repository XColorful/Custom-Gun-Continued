package dev.xcolorful.customgun.core.api.resource.data.tag;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public enum BlockType {
    GLASS_BLOCK(BlockTag.GLASS_BLOCK);

    public final TagKey<Block> tagKey;
    BlockType(String tag) {
        this.tagKey = BlockTags.create(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s",
                CustomGun.MOD_ID,
                tag)));
    }

    public TagKey<Block> getTagKey() {
        return this.tagKey;
    }
}
