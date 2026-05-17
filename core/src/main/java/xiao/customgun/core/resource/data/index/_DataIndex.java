/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index;

import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class _DataIndex<T extends _DataIndex<T>> extends ResourcePojo<T> {

    private String nameLang;
    private String tooltipLang;

    private ResourceLocation dataLocation;
    private ResourceLocation displayIndexLocation;

    private int slotSort = 0;

    // --------Getter & Setter--------

    public final String getNameLang() {
        return nameLang;
    }
    public final String getTooltipLang() {
        return tooltipLang;
    }
    public final ResourceLocation getDataLocation() {
        return dataLocation;
    }
    public final ResourceLocation getDisplayIndexLocation() {
        return displayIndexLocation;
    }
    public final int getSlotSort() {
        return slotSort;
    }

    public final void setNameLang(String nameLang) {
        this.nameLang = nameLang;
    }
    public final void setTooltipLang(String tooltipLang) {
        this.tooltipLang = tooltipLang;
    }
    public final void setDataLocation(ResourceLocation dataLocation) {
        this.dataLocation = dataLocation;
    }
    public final void setDisplayIndexLocation(ResourceLocation displayIndexLocation) {
        this.displayIndexLocation = displayIndexLocation;
    }
    public final void setSlotSort(int slotSort) {
        this.slotSort = slotSort;
    }
}