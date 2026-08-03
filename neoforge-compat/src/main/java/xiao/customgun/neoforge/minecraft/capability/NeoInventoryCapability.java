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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;

public class NeoInventoryCapability implements IInventoryCapability {

    private final @NotNull ResourceHandler<ItemResource> itemHandler;
    private NeoInventoryCapability(@NotNull ResourceHandler<ItemResource> itemHandler) {
        this.itemHandler = itemHandler;
    }
    public static NeoInventoryCapability fromLivingEntity(@Nullable LivingEntity livingEntity, @Nullable Direction facing) {
        if (livingEntity == null) return null;
        ResourceHandler<ItemResource> itemHandler;
        if (facing != null) itemHandler = livingEntity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, facing);
        else itemHandler = livingEntity.getCapability(Capabilities.Item.ENTITY, null);
        
        if (itemHandler != null) return new NeoInventoryCapability(itemHandler);
        else return null;
    }

    @Override
    public int getContainerSize() {
        return this.itemHandler.size();
    }

    @Override
    public @NotNull ItemStack getItemReadOnly(int slot) {
        ItemResource resource = this.itemHandler.getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int amount = this.itemHandler.getAmountAsInt(slot);
        return resource.toStack(amount);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 转换成无数量限制的 ItemResource 标签
        ItemResource resource = ItemResource.of(stack);
        int originalCount = stack.getCount();

        // 开启事务机制进行数据操作
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = this.itemHandler.insert(slot, resource, originalCount, transaction);

            // 如果不是模拟，就真正提交这次改动
            if (!simulate) {
                transaction.commit();
            }

            // 计算并返回剩余未能塞入的残余物品栈
            int remainder = originalCount - inserted;
            return remainder <= 0 ? ItemStack.EMPTY : stack.copyWithCount(remainder);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        // 先提取出这个格子的物品类型
        ItemResource resource = this.itemHandler.getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 开启事务机制模拟或真的进行扣除
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = this.itemHandler.extract(slot, resource, amount, transaction);

            if (!simulate) {
                transaction.commit();
            }

            // 将成功取出的资源重新打包为 ItemStack
            return resource.toStack(extracted);
        }
    }

    @Override
    public int getMaxStackSize(int slot) {
        return this.itemHandler.getCapacityAsInt(slot, ItemResource.EMPTY);
    }

    @Override
    public boolean canReplaceItem(int slot, @NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        return this.itemHandler.isValid(slot, ItemResource.of(stack));
    }
}
