/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.recipe.recipe._TableResultData;

public class TableResult {

    private _TableResultData pojo;

    private ItemStack resultItem = ItemStack.EMPTY;
    private Identifier tabLocation;

    private @Nullable _TableResultRaw raw;

    public TableResult(ItemStack resultItem, @Nullable Identifier tabLocation) {
        this(null, resultItem, tabLocation, null);
    }
    public TableResult(@NotNull _TableResultRaw raw) {
        this(null, null, null, raw);
    }
    public TableResult(_TableResultData pojo, ItemStack resultItem, @Nullable Identifier tabLocation, @Nullable _TableResultRaw raw) {
        this.pojo = pojo;
        this.resultItem = resultItem;
        this.tabLocation = tabLocation; // TODO 换成注册的Tab
        this.raw = raw;
    }
    public static TableResult fromPojo(_TableResultData pojo) {
        return new TableResult(pojo, null, null, null);
    }

    public _TableResultData getPojo() {
        return this.pojo;
    }

    public void init() {
        if (this.raw != null) {
            // TODO
            this.raw = null;
        }
    }

    public ItemStack getResultItem() {
        return this.resultItem;
    }
    public Identifier getTabLocation() {
        return this.tabLocation;
    }
}
