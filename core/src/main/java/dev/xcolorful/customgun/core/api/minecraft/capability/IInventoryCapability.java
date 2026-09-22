package dev.xcolorful.customgun.core.api.minecraft.capability;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 等价于 net.minecraftforge.items.IItemHandler
 */
public interface IInventoryCapability {

    int getContainerSize();

    /**
     * 只读getter
     * @since 1.21.10 返回的ItemStack是副本，如可能造成NBT变化则需要改用{@link #extractItem}再{@link #insertItem}
     */
    @NotNull ItemStack getItemReadOnly(int slot);

    @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    @NotNull ItemStack extractItem(int slot, int amount, boolean simulate);

    int getMaxStackSize(int slot);

    boolean canReplaceItem(int slot, @NotNull ItemStack stack);
}
