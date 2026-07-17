/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.tab.TabGroup;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.recipe.recipe._TableResultData;

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
        return new TableResult(pojo, null, null, null);
    }

    public _TableResultData getPojo() {
        return this.pojo;
    }

    public void prepare() {
        if (this.raw != null) {
            TableResult tableResult = this.raw.getTableResultOrEmpty();
            this.resultItem = tableResult.getResultItem();
            if (this.tabGroupLocation == null || this.tabGroupLocation.equals(ResourceTag.NULL_LOCATION)) {
                this.tabGroupLocation = tableResult.getTabGroupLocation();
            }
            this.raw = null;
        }
    }

    public ItemStack getResultItem() {
        return this.resultItem;
    }
    public ResourceLocation getTabGroupLocation() {
        return this.tabGroupLocation;
    }
}
