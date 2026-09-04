/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.recipe;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.tab.TabGroup;
import dev.xcolorful.customgun.core.api.recipe.RecipeResultType;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.data.recipe.recipe._TableResultData;
import dev.xcolorful.customgun.core.resource.data.recipe.recipe.result._ResultItemData;
import dev.xcolorful.customgun.core.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TableResult {

    private _TableResultData pojo;

    private ItemStack resultItem = ItemStack.EMPTY;
    /**
     * 暂不使用 {@link TabGroup}
     */
    private ResourceLocation tabGroupLocation;

    private @Nullable _TableResultRaw raw;

    public TableResult(ItemStack resultItem, @Nullable ResourceLocation tabGroupLocation) {
        this(null, resultItem, tabGroupLocation, null);
    }
    public TableResult(@NotNull _TableResultRaw raw) {
        this(null, null, null, raw);
    }
    public TableResult(_TableResultData pojo, ItemStack resultItem, @Nullable ResourceLocation tabGroupLocation, @Nullable _TableResultRaw raw) {
        this.pojo = pojo;
        this.resultItem = resultItem;
        this.tabGroupLocation = tabGroupLocation;
        this.raw = raw;
    }
    public static TableResult fromPojo(_TableResultData pojo) {
        if (!pojo.isValid()) {
            return new TableResult(pojo, ItemStack.EMPTY, pojo.getTabGroupLocation(), null);
        }

        int resultCount = Math.max(1, pojo.getResultCount());
        RecipeResultType resultType = pojo.getResultType();
        return switch (resultType) {
            case GUN, ATTACHMENT, AMMO -> {
                @Nullable var pojoLocation = pojo.getPojoLocation();
                if (pojoLocation == null) pojoLocation = ResourceTag.NULL_LOCATION;

                yield new TableResult(pojo, null, pojo.getTabGroupLocation(), new _TableResultRaw(resultType, pojoLocation, resultCount));
            }
            case CUSTOM -> new TableResult(pojo, _buildCustomTableResult(pojo, resultCount), pojo.getTabGroupLocation(), null);
            // 增加类型使此处强制编译不通过
        };
    }
    private static ItemStack _buildCustomTableResult(_TableResultData pojo, int resultCount) {
        @Nullable _ResultItemData resultItem = pojo.getResultItem();
        if (resultItem == null) return ItemStack.EMPTY;

        var itemLocation = resultItem.getItemLocation();
        @Nullable Item item = CustomGun.getMcRegistry().getItem(itemLocation);
        if (item == null) {
            CustomGun.LOGGER.warn("TableResult: Item {} not found for custom recipe result", itemLocation);
            return ItemStack.EMPTY;
        }

        ItemStack itemStack = new ItemStack(item, resultCount); {
            CompoundTag customDataTag = resultItem.getItemNbt();
            if (!customDataTag.isEmpty()) {
                NBTUtils.setCustomDataTag(itemStack, customDataTag);
            }
        }

        return itemStack;
    }

    public _TableResultData getPojo() {
        return this.pojo;
    }

    public void prepare() {
        if (this.raw == null) return;

        TableResult tableResult = this.raw.prepareTableResultOrEmpty(); {
            this.resultItem = tableResult.getResultItem();
            if (this.tabGroupLocation == null || this.tabGroupLocation.equals(ResourceTag.NULL_LOCATION)) {
                this.tabGroupLocation = tableResult.getTabGroupLocation();
            }
        }
        this.raw = null;
    }

    public @NotNull ItemStack getResultItem() {
        return this.resultItem != null ? this.resultItem : ItemStack.EMPTY;
    }
    public @NotNull ResourceLocation getTabGroupLocation() {
        return this.tabGroupLocation != null ? this.tabGroupLocation : TabGroup.GUN_CUSTOM.registryLocation;
    }
}
