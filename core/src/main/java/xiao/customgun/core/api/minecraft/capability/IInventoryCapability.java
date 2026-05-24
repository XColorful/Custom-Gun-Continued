package xiao.customgun.core.api.minecraft.capability;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 等价于 net.minecraftforge.items.IItemHandler
 */
public interface IInventoryCapability {

    int getContainerSize();

    /**
     * 只读getter
     */
    @NotNull ItemStack getItemReadOnly(int slot);

    @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    @NotNull ItemStack extractItem(int slot, int amount, boolean simulate);

    int getMaxStackSize(int slot);

    boolean canReplaceItem(int slot, @NotNull ItemStack stack);
}
