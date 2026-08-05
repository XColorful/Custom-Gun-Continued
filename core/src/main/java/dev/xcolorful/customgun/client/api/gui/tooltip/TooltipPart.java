package dev.xcolorful.customgun.client.api.gui.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface TooltipPart<T extends BaseTooltipContext<?>> {

    void build(T context);

    int measureHeight(T context);

    void renderText(T context,
                    GuiGraphics guiGraphics,
                    Font font, int pX, int pY);

    void renderImage(T context,
                     Font font, int pX, int pY,
                     int width, int height,
                     GuiGraphics guiGraphics);
}
