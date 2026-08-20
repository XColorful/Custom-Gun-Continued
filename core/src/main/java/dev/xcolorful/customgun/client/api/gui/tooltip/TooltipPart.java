package dev.xcolorful.customgun.client.api.gui.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface TooltipPart<T extends BaseTooltipContext<?>> {

    void build(T context);

    int measureHeight(T context);

    default void renderText(T context,
                            GuiGraphicsExtractor guiGraphics,
                            Font font,
                            int pX, int pY) {
    }

    default void renderImage(T context,
                             Font font,
                             int pX, int pY,
                             int width, int height,
                             GuiGraphicsExtractor guiGraphics) {
    }
}
