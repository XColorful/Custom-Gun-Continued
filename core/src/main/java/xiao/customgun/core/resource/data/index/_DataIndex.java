/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.ComponentUtils;

public abstract class _DataIndex<T extends _DataIndex<T>> extends ResourcePojo<T> {

    private MutableComponent nameLang;
    private MutableComponent tooltipLang;

    private Identifier dataLocation;
    private Identifier displayIndexLocation;

    private int slotSort = 0;

    @Override
    protected void validatePojo() {
        boolean n1 = (this.dataLocation == null | this.displayIndexLocation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        if (this.nameLang == null) this.nameLang = ComponentUtils.unknownTranslatableKey();
        if (this.tooltipLang == null) this.tooltipLang = ComponentUtils.unknownTranslatableKey();
        if (this.slotSort < 0) this.slotSort = 0;
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public final MutableComponent getNameLang() {
        return nameLang;
    }
    public final MutableComponent getTooltipLang() {
        return tooltipLang;
    }
    public final Identifier getDataLocation() {
        return dataLocation;
    }
    public final Identifier getDisplayIndexLocation() {
        return displayIndexLocation;
    }
    public final int getSlotSort() {
        return slotSort;
    }

    public final void setNameLang(MutableComponent nameLang) {
        this.nameLang = nameLang;
    }
    public final void setTooltipLang(MutableComponent tooltipLang) {
        this.tooltipLang = tooltipLang;
    }
    public final void setDataLocation(Identifier dataLocation) {
        this.dataLocation = dataLocation;
    }
    public final void setDisplayIndexLocation(Identifier displayIndexLocation) {
        this.displayIndexLocation = displayIndexLocation;
    }
    public final void setSlotSort(int slotSort) {
        this.slotSort = slotSort;
    }
}