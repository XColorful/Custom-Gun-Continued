package dev.xcolorful.customgun.client.api.gui.tooltip;

import net.minecraft.client.gui.Font;

public interface TooltipPart<T extends BaseTooltipContext<?>> {

    void build(T context,
               Font font);

    int measureHeight(T context);

    default void renderText(T context,
                            int startX, int startY) {
    }

    default void renderImage(T context,
                             int startX, int startY) {
    }
}
