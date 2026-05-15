package xiao.customgun.core.resource.data.index;

import net.minecraft.resources.Identifier;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class _DataIndex<T extends _DataIndex<T>> extends ResourcePojo<T> {

    private String nameLang;
    private String tooltipLang;

    private Identifier dataLocation;
    private Identifier displayIndexLocation;

    private int slotSort = 0;

    // --------Getter & Setter--------

    public final String getNameLang() {
        return nameLang;
    }
    public final String getTooltipLang() {
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

    public final void setNameLang(String nameLang) {
        this.nameLang = nameLang;
    }
    public final void setTooltipLang(String tooltipLang) {
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