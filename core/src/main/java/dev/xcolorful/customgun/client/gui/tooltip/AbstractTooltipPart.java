package dev.xcolorful.customgun.client.gui.tooltip;

import dev.xcolorful.customgun.client.api.gui.GuiSize;

public abstract class AbstractTooltipPart {

    public static final int textLineHeight = GuiSize.Text.TEXT_LINE_HEIGHT;
    public static final int textLineSpacing = GuiSize.Text.TEXT_LINE_SPACING;

    public static final int itemHeight = GuiSize.Item.ITEM_HEIGHT;
    public static final int itemWidth = GuiSize.Item.ITEM_WIDTH;

    public static final int packedLightCoords = 0xF000F0;

    public AbstractTooltipPart() {}
}
