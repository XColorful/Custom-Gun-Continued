package xiao.customgun.client.api.gui.tooltip;

import org.jetbrains.annotations.NotNull;

public abstract class BaseTooltipContext<T extends BaseTooltipView> {

    public final @NotNull T view;
    private int maxWidth = 0;
    public BaseTooltipContext(@NotNull T view) {
        this.view = view;
    }

    protected abstract void buildView();

    protected abstract int calculateHeight();

    // --------Getter & Setter--------

    public final int getHeight() {
        return this.calculateHeight();
    }
    public final int getMaxWidth() {
        return this.maxWidth;
    }

    public final void widenMaxWidth(int maxWidth) {
        this.maxWidth = Math.max(this.maxWidth, maxWidth);
    }
}
