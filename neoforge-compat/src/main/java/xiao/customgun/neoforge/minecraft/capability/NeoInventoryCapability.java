/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.neoforge.minecraft.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;

public class NeoInventoryCapability implements IInventoryCapability {

    private final @NotNull IItemHandler itemHandler;
    private NeoInventoryCapability(@NotNull IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }
    public static NeoInventoryCapability fromLivingEntity(LivingEntity livingEntity, @Nullable Direction facing) {
        IItemHandler itemHandler;
        if (facing != null) itemHandler = livingEntity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, facing);
        else itemHandler = livingEntity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (itemHandler != null) return new NeoInventoryCapability(itemHandler);
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
