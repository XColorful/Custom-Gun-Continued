/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.index;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.resources.ResourceLocation;

public abstract class _DataIndex<T extends _DataIndex<T>> extends ResourcePojo<T> {

    private String nameLang;
    private String tooltipLang;

    private ResourceLocation dataLocation;
    private ResourceLocation displayIndexLocation;

    private int slotSort = 0;

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.dataLocation == null | this.displayIndexLocation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        if (this.nameLang == null) this.nameLang = ComponentUtils.UNKNOWN_TRANSLATABLE_KEY;
        if (this.tooltipLang == null) this.tooltipLang = ComponentUtils.UNKNOWN_TRANSLATABLE_KEY;
        if (this.slotSort < 0) this.slotSort = 0;
        this.setValid(true);
    }

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

    // --------Back compatibility--------

    @SuppressWarnings("unchecked")
    @Override
    public T applyBackCompatibility() {
        this.dataLocation = this.dataLocation == null ? ResourceTag.NULL_LOCATION : this.dataLocation;
        this.displayIndexLocation = this.displayIndexLocation == null ? ResourceTag.NULL_LOCATION : this.displayIndexLocation;
        return (T) this;
    }
}