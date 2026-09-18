package dev.xcolorful.customgun.core.api.item.block;

import dev.xcolorful.customgun.core.api.item.IBlock;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IBlockGetter {

    static @Nullable IBlock fromItemStack(@Nullable ItemStack blockItem) {
        if (blockItem == null) return null;
        return blockItem.getItem() instanceof IBlock iBlock ? iBlock : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IBlock getIBlockOrNull(@Nullable ItemStack blockItem) {
        return fromItemStack(blockItem);
    }
}
