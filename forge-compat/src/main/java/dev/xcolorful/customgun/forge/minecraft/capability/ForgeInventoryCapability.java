package dev.xcolorful.customgun.forge.minecraft.capability;

import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeInventoryCapability implements IInventoryCapability {

    private final @NotNull IItemHandler itemHandler;
    private ForgeInventoryCapability(@NotNull IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }
    public static ForgeInventoryCapability fromLivingEntity(@Nullable LivingEntity livingEntity, @Nullable Direction facing) {
        if (livingEntity == null) return null;
        IItemHandler itemHandler = livingEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing).orElse(null);
        if (itemHandler != null) return new ForgeInventoryCapability(itemHandler);
        else return null;
    }

    @Override
    public int getContainerSize() {
        return this.itemHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getItemReadOnly(int slot) {
        return this.itemHandler.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.itemHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.itemHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getMaxStackSize(int slot) {
        return this.itemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean canReplaceItem(int slot, @NotNull ItemStack stack) {
        return this.itemHandler.isItemValid(slot, stack);
    }
}
